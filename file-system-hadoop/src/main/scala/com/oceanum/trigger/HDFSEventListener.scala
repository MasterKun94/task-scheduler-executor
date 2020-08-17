package com.oceanum.trigger

import java.net.URI
import java.util.Date
import java.util.concurrent.{ConcurrentSkipListSet, Executors}

import com.oceanum.common.Environment
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hdfs.client.HdfsAdmin
import org.apache.hadoop.hdfs.inotify.Event
import org.apache.hadoop.hdfs.inotify.Event.EventType

import scala.collection.concurrent.TrieMap
import scala.concurrent.ExecutionContext
import scala.util.matching.Regex

class HDFSEventListener {
  private val admin = new HdfsAdmin(URI.create(Environment.HADOOP_FS_URL), new Configuration())
  private val ec = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())
  private val ignoredUsers: Set[String] = Set()
  private val ignoredGroups: Set[String] = Set()
  private val patterns: TrieMap[String, (Regex, Event.CreateEvent => Unit)] = TrieMap()
  @volatile
  var isRunning: Boolean = true

  def start(): Unit = {
    System.setProperty("HADOOP_USER_NAME", Environment.HADOOP_USER)
    val eventStream = admin.getInotifyEventStream()
    ec.execute(new Runnable {
      override def run(): Unit = {
        while (isRunning) {
          eventStream.take().getEvents
            .filter(_.getEventType == EventType.CREATE)
            .map(_.asInstanceOf[Event.CreateEvent])
            .filterNot(e => ignoredUsers.contains(e.getOwnerName) || ignoredGroups.contains(e.getGroupName))
            .foreach { e =>
              for ((regex, handle) <- patterns.values) {
                regex.findFirstMatchIn(e.getPath) match {
                  case Some(_) => handle(e)
                  case None =>
                }
              }
            }

        }
      }
    })
  }

  def add(name: String, regex: Regex, startTime: Option[Date])(handle: Event.CreateEvent => Unit): Unit = {
    patterns += (name -> (regex, handle))
  }

  def remove(name: String): Unit = {
    patterns -= name
  }

  def list: Iterable[String] = patterns.keys

  def stop(): Unit = {
    isRunning = false
  }
}
