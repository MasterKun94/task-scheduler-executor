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

  override def download(srcPath: String, destPath: String)(implicit ex: ExecutionContext): Future[Unit] = {
    Future {
      HDFSFileClient.download(srcPath, destPath)
    }
  }

  override def upload(srcPath: String, destPath: String)(implicit ex: ExecutionContext): Future[Unit] = {
    Future {
      HDFSFileClient.upload(srcPath, destPath)
    }
  }
}

object HDFSFileClient {
  private val fileSystem = {
    System.setProperty("hadoop.home.dir", Environment.HADOOP_HOME)
    val configuration = new Configuration()
    FileSystem.setDefaultUri(configuration, new URI(Environment.HADOOP_FS_URL))
    FileSystem.get(new URI(Environment.HADOOP_FS_URL), configuration, Environment.HADOOP_USER)
  }

  def upload(srcPath: String, destPath: String): Unit = {
    val src = new Path(srcPath)
    val dst = new Path(destPath)
    fileSystem.copyFromLocalFile(src, dst)
  }

  def download(srcPath: String, destPath: String): Unit = {
    val src = new Path(srcPath)
    val dst = new Path(destPath)
    fileSystem.copyToLocalFile(src, dst)
  }

  def exist(path: String): Boolean = {
    fileSystem.exists(new Path(path))
  }

  def isDir(path: String): Boolean = {
    fileSystem.isDirectory(new Path(path))
  }

  def isFile(path: String): Boolean = {
    fileSystem.isFile(new Path(path))
  }
}