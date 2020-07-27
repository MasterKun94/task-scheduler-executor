package com.oceanum.trigger

import java.net.URI
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

import com.oceanum.common.Environment
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hdfs.client.HdfsAdmin
import org.apache.hadoop.hdfs.inotify.Event.{AppendEvent, CloseEvent, CreateEvent, MetadataUpdateEvent, RenameEvent, UnlinkEvent}

/**
 * @author chenmingkun
 * @date 2020/7/26
 */
class HDFSEventListener {

  private val running: AtomicBoolean = new AtomicBoolean(true)

  private val admin: HdfsAdmin = {
    println(Environment.HADOOP_HOME)
    System.setProperty("hadoop.home.dir", Environment.HADOOP_HOME)
    System.setProperty("HADOOP_USER_NAME", "hdfs")
    val configuration = new Configuration()
    new HdfsAdmin(URI.create("hdfs://192.168.10.136:8022"), configuration)
  }

  def create(): Unit = {
    val eventStream = admin.getInotifyEventStream()
    while (running.get()) {
      eventStream.take() match {
//        case event: CreateEvent =>
//        case event: CloseEvent =>
//        case event: AppendEvent =>
//        case event: RenameEvent =>
//        case event: MetadataUpdateEvent =>
//        case event: UnlinkEvent =>
        case event => println(event)
      }
    }
  }

  def close(): Unit = running.set(false)
}

object HDFSEventListener {
  def main(args: Array[String]): Unit = {
    Environment.loadArgs(args :+ s"--conf=src/main/resources/application.properties")
    new HDFSEventListener().create()
  }
}