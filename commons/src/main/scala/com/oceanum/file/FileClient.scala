package com.oceanum.file

import java.net.URI

import com.oceanum.common.Environment

import scala.concurrent.{ExecutionContext, Future}

/**
 * @author chenmingkun
 * @date 2020/7/4
 */
abstract class FileClient(val scheme: String) {
  def download(srcPath: String, destPath: String)(implicit ec: ExecutionContext): Future[Unit]

  def upload(srcPath: String, destPath: String)(implicit ec: ExecutionContext): Future[Unit]
}

object FileClient extends FileClient("root") {
  lazy val innerClients: Map[String, FileClient] = Environment.FILE_CLIENT_CLASSES
    .map(_.getConstructor().newInstance().asInstanceOf[FileClient])
    .map(c => (c.scheme, c))
    .toMap

  override def download(srcPath: String, destPath: String)(implicit ec: ExecutionContext): Future[Unit] = {
    val uri = new URI(srcPath)
    val scheme = if (uri.getScheme == null || uri.getScheme.equals("null")) Environment.FILE_CLIENT_DEFAULT_SCHEME else uri.getScheme
    innerClients(scheme).download(srcPath, destPath)
  }

  override def upload(srcPath: String, destPath: String)(implicit ec: ExecutionContext): Future[Unit] = {
    val uri = new URI(destPath)
    val scheme = if (uri.getScheme == null || uri.getScheme.equals("null")) Environment.FILE_CLIENT_DEFAULT_SCHEME else uri.getScheme
    innerClients(scheme).upload(srcPath, destPath)
  }
}
