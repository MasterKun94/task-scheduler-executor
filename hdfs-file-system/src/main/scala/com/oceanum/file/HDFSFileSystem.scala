package com.oceanum.file

import java.io.{InputStream, OutputStream}
import java.net.URI

import com.oceanum.annotation.IFileSystem
import com.oceanum.common.Environment
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{Path, FileSystem => HdfsFs}

import scala.concurrent.{ExecutionContext, Future}

/**
 * @author chenmingkun
 * @date 2020/7/5
 */
@IFileSystem
class HDFSFileSystem extends FileSystem("hdfs") {

  override def download(srcPath: String, destPath: String)(implicit ex: ExecutionContext): Future[Unit] = {
    Future {
      HDFSFileSystem.download(srcPath, destPath)
    }
  }

  override def upload(srcPath: String, destPath: String)(implicit ex: ExecutionContext): Future[Unit] = {
    Future {
      HDFSFileSystem.upload(srcPath, destPath)
    }
  }
}

object HDFSFileSystem {
  private val fileSystem = {
    System.setProperty("hadoop.home.dir", Environment.HADOOP_HOME)
    val configuration = new Configuration()
    HdfsFs.setDefaultUri(configuration, new URI(Environment.HADOOP_FS_URL))
    HdfsFs.get(new URI(Environment.HADOOP_FS_URL), configuration, Environment.HADOOP_USER)
  }

  def uploadFileStream(destPath: String): OutputStream = fileSystem.create(new Path(destPath))

  def downloadFileStream(srcPath: String): InputStream = fileSystem.open(new Path(srcPath))

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

  def size(path: String): Long = {
    fileSystem.getFileStatus(new Path(path)).getLen
  }

  def accessTime(path: String): Long = {
    fileSystem.getFileStatus(new Path(path)).getAccessTime
  }

  def modifiedTime(path: String): Long = {
    fileSystem.getFileStatus(new Path(path)).getModificationTime
  }

  def owner(path: String): String = {
    fileSystem.getFileStatus(new Path(path)).getOwner
  }

  def blockSize(path: String): Long = {
    fileSystem.getFileStatus(new Path(path)).getBlockSize
  }
}