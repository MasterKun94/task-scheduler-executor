package com.oceanum.file

import java.io.File
import java.net.URI

import com.oceanum.common.{Environment, WINDOWS}

import scala.concurrent.Future
import com.oceanum.common.Implicits.PathHelper

/**
 * @author chenmingkun
 * @date 2020/7/4
 */
abstract class FileClient(val scheme: String) {
  def download(srcPath: String, destPath: String): Future[Unit]
}

object FileClient extends FileClient("root") {
  lazy val innerClients: Map[String, FileClient] = Environment.FILE_CLIENT_CLASSES
    .map(_.getConstructor().newInstance().asInstanceOf[FileClient])
    .map(c => (c.scheme, c))
    .toMap

  override def download(srcPath: String, destPath: String): Future[Unit] = {
    val path0 = srcPath.toPath("/")
    val path = if (new File(path0).isAbsolute && Environment.OS == WINDOWS) "/" + path0 else path0
    val uri = new URI(path)
    val scheme = if (uri.getScheme == null || uri.getScheme.equals("null")) Environment.FILE_CLIENT_DEFAULT_SCHEME else uri.getScheme
    innerClients(scheme).download(path, destPath)
  }
}
