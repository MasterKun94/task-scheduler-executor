package com.oceanum.file

import java.net.URI

import scala.concurrent.Future

/**
 * @author chenmingkun
 * @date 2020/7/4
 */
abstract class FileClient(val schema: String) {
  def download(srcPath: String, destPath: String): Future[Unit]
}

object FileClient extends FileClient("root") {
  lazy val clientClasses: Seq[String] = Seq("com.oceanum.file.ClusterFileClient")
  lazy val innerClients: Map[String, FileClient] = clientClasses
    .map(Class.forName)
    .map(_.getConstructor().newInstance().asInstanceOf[FileClient])
    .map(c => (c.schema, c))
    .toMap

  override def download(srcPath: String, destPath: String): Future[Unit] = {
    innerClients(new URI(srcPath).getScheme).download(srcPath, destPath)
  }
}
