package com.oceanum.file
import scala.concurrent.Future

/**
 * @author chenmingkun
 * @date 2020/7/5
 */
class HDFSFileClient extends FileClient("hdfs") {
  override def download(srcPath: String, destPath: String): Future[Unit] = {
    ???
  }
}
