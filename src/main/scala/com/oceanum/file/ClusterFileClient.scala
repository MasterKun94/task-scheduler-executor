package com.oceanum.file
import java.net.URI

import com.oceanum.common.Environment

import scala.concurrent.Future

/**
 * @author chenmingkun
 * @date 2020/7/5
 */
class ClusterFileClient extends FileClient("cluster") {
  override def download(srcPath: String, destPath: String): Future[Unit] = {
    val uri = new URI(srcPath)
    val host = if (uri.getHost == null || uri.getHost.equals("null")) Environment.HOST else uri.getHost
    val port = if (uri.getPort < 0) Environment.FILE_SERVER_PORT else uri.getPort
    val path = uri.getPath
    ClusterFileServerApi.download(host + ":" + port, path, destPath)
  }
}
