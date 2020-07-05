package com.oceanum.file

import java.io.File
import java.net.URI

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
  lazy val clientClasses: Seq[String] = Seq("com.oceanum.file.ClusterFileClient")
  lazy val innerClients: Map[String, FileClient] = clientClasses
    .map(Class.forName)
    .map(_.getConstructor().newInstance().asInstanceOf[FileClient])
    .map(c => (c.scheme, c))
    .toMap

  override def download(srcPath: String, destPath: String): Future[Unit] = {
    val path = srcPath.toPath("/")
    innerClients(new URI(path).getScheme).download(path, destPath)
  }
}
