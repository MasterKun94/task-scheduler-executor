package com.oceanum.file
import java.net.{URI, URL}

import scala.concurrent.Future

/**
 * @author chenmingkun
 * @date 2020/7/5
 */
class ClusterFileClient extends FileClient("cluster") {
  override def download(srcPath: String, destPath: String): Future[Unit] = {
    val uri = new URI(srcPath)
    val host = if (uri.getPort < 0) uri.getHost
    else  uri.getHost + ":" + uri.getPort
    ClusterFileServerApi.download(host, uri.getPath, destPath)
  }
}
