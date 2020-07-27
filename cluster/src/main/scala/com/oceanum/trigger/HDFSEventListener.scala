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
  private val admin: HdfsAdmin = new HdfsAdmin(URI.create(Environment.HADOOP_FS_URL), new Configuration())

  def create(): Unit = {
    val eventStream = admin.getInotifyEventStream()
    while (running.get()) {
      eventStream.poll(60, TimeUnit.SECONDS) match {
        case event: CreateEvent =>
        case event: CloseEvent =>
        case event: AppendEvent =>
        case event: RenameEvent =>
        case event: MetadataUpdateEvent =>
        case event: UnlinkEvent =>
        case event =>
      }
    }
  }

  def close(): Unit = running.set(false)
}
