package com.oceanum.trigger

import java.net.URI
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

import com.oceanum.common.Environment
import com.oceanum.file.HDFSFileClient
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hdfs.client.HdfsAdmin
import org.apache.hadoop.hdfs.inotify.Event.{AppendEvent, CloseEvent, CreateEvent, MetadataUpdateEvent, RenameEvent, UnlinkEvent}

import scala.concurrent.ExecutionContext

/**
 * @author chenmingkun
 * @date 2020/7/26
 */
class HDFSEventListener {

  private val running: AtomicBoolean = new AtomicBoolean(true)

  private val admin: HdfsAdmin = {
    println(Environment.HADOOP_HOME)
    println(Environment.HADOOP_FS_URL)
    System.setProperty("hadoop.home.dir", Environment.HADOOP_HOME)
    System.setProperty("HADOOP_USER_NAME", "hdfs")
    val configuration = new Configuration()
    new HdfsAdmin(URI.create(Environment.HADOOP_FS_URL), configuration)
  }

  def create(): Unit = {
    val eventStream = admin.getInotifyEventStream()
    while (running.get()) {
      eventStream.take() match {
        case event: CreateEvent =>
        case event: CloseEvent =>
        case event: AppendEvent =>
        case event: RenameEvent =>
        case event: MetadataUpdateEvent =>
        case event: UnlinkEvent =>
        case event => println(event)
      }
    }
  }

  def close(): Unit = running.set(false)
}

object HDFSEventListener {
  def main(args: Array[String]): Unit = {
    Environment.loadArgs(args :+ s"--conf=cluster/src/main/resources/application.properties")
    new HDFSEventListener().create()
  }
}

object HDFSTest {
  def main(args: Array[String]): Unit = {
    Environment.loadArgs(args :+ s"--conf=cluster/src/main/resources/application.properties")
    println(Environment.HADOOP_HOME)
    println(Environment.HADOOP_FS_URL)
    val client = new HDFSFileClient()
    client.upload("E:\\chenmingkun\\task-scheduler-executor\\cluster\\src\\main\\resources\\python.py", "/tmp/")(ExecutionContext.global)
    Thread.sleep(10000)
  }
}