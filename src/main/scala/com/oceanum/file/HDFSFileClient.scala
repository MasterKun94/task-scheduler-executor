package com.oceanum.file

import java.io.FileOutputStream
import java.net.URI

import com.oceanum.HDFSClient
import com.oceanum.common.Environment
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.io.IOUtils

import scala.concurrent.{ExecutionContext, Future}

/**
 * @author chenmingkun
 * @date 2020/7/5
 */
class HDFSFileClient extends FileClient("hdfs") {
  private lazy val fileSystem = {
    System.setProperty("hadoop.home.dir", Environment.HADOOP_HOME)
    val configuration = new Configuration()
    FileSystem.get(new URI(Environment.HADOOP_FS_URL), configuration, Environment.HADOOP_USER)
  }

  override def download(srcPath: String, destPath: String): Future[Unit] = {
    implicit val ec: ExecutionContext = Environment.GLOBAL_EXECUTOR
    Future {
      downloadFile(srcPath, destPath)
    }
  }

  private def downloadFile(srcPath: String, destPath: String): Unit = {
    val src = new Path(srcPath)
    val dst = new Path(destPath)
    if (fileSystem.isFile(src)) {
      fileSystem.copyToLocalFile(src, dst)
    } else {
      val iterator = fileSystem.listFiles(src, true)
      while (iterator.hasNext) {
        val fStatus = iterator.next()

      }
    }
  }
}

object HDFSFileClient {
  def main(args: Array[String]): Unit = {
    Environment.loadArgs(Array("--conf=src/main/resources/application.properties"))
    HDFSClient.main(args)
//    import com.oceanum.common.Implicits.PathHelper
//    new HDFSFileClient().download(
//      "hdfs://192.168.10.136:8022/tmp/chenmingkun/test",
//      "C:\\Users\\chenmingkun\\work\\idea\\work\\task-scheduler-core\\task-scheduler-executor\\src\\main\\resources")
//        .onComplete(println)(ExecutionContext.global)
//
//    Thread.sleep(20000)
  }
}
