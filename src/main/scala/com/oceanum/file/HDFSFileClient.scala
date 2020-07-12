package com.oceanum.file

import java.net.URI

import com.oceanum.common.Environment
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}

import scala.concurrent.{ExecutionContext, Future}

/**
 * @author chenmingkun
 * @date 2020/7/5
 */
class HDFSFileClient extends FileClient("hdfs") {
  implicit val ec: ExecutionContext = Environment.GLOBAL_EXECUTOR
  private lazy val fileSystem = {
    System.setProperty("hadoop.home.dir", Environment.HADOOP_HOME)
    val configuration = new Configuration()
    FileSystem.setDefaultUri(configuration, new URI(Environment.HADOOP_FS_URL))
    FileSystem.get(new URI(Environment.HADOOP_FS_URL), configuration, Environment.HADOOP_USER)
  }

  override def download(srcPath: String, destPath: String): Future[Unit] = {
    val src = new Path(srcPath)
    val dst = new Path(destPath)
    Future {
      fileSystem.copyToLocalFile(src, dst)
    }
  }

  override def upload(srcPath: String, destPath: String): Future[Unit] = {
    val src = new Path(srcPath)
    val dst = new Path(destPath)
    Future {
      fileSystem.copyFromLocalFile(src, dst)
    }
  }
}
