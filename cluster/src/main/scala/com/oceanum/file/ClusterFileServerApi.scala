package com.oceanum.file

import java.io.File
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicInteger

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.scaladsl.{FileIO, Sink, Source}
import akka.stream.{ActorAttributes, ActorMaterializer}
import akka.util.ByteString
import com.oceanum.common.{Environment, Log, ActorSystems}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

/**
 * @author chenmingkun
 * @date 2020/6/25
 */
object ClusterFileServerApi extends Log(ActorSystems.FILE_SERVER_SYSTEM) {

  private implicit lazy val mat: ActorMaterializer = ActorMaterializer()
  private lazy val http = Http()

  def transfer(srcHost: String, srcPath: String, destHost: String, destPath: String)(implicit ec: ExecutionContext): Future[Unit] = {
    val srcHost0 = getHost(srcHost)
    val destHost0 = getHost(destHost)
    val request = HttpRequest(HttpMethods.PUT, uri = s"http://$destHost0/${Environment.FILE_SERVER_CONTEXT_PATH}/$destPath")
    val data = HttpEntity(
      ContentTypes.`text/plain(UTF-8)`,
      s"host=$srcHost${ClusterFileServer.sep}path=$srcPath"
    )
    http
      .singleRequest(request.copy(entity = data))
      .flatMap {
        case HttpResponse(StatusCodes.OK, _, entity, _) =>
          entity.dataBytes
            .map(_.utf8String)
            .runForeach(str => log.info(s"Transfer file success from: [$srcHost0/$srcPath] to: [$destHost0/$str]"))
            .map(_ => Unit)
        case r: HttpResponse =>
          r.discardEntityBytes()
          Future.failed(new Exception(s"Transfer file failed, response: ${r.toString()}"))
        case _ =>
          Future.failed(new Exception("Unable to transfer file!"))
      }
  }

  def delete(host: String, path: String)(implicit ec: ExecutionContext): Future[Unit] = {
    val host0 = getHost(host)
    val request = HttpRequest(HttpMethods.DELETE, uri = s"http://$host0/${Environment.FILE_SERVER_CONTEXT_PATH}/$path")
    http
      .singleRequest(request)
      .flatMap {
        case HttpResponse(StatusCodes.OK, _, entity, _) =>
          entity.dataBytes
            .map(_.utf8String)
            .runForeach(str => log.info(s"Delete file success from: [$host0/$path]"))
            .map(_ => Unit)
        case r: HttpResponse =>
          r.discardEntityBytes()
          Future.failed(new Exception(s"Delete file failed, response: ${r.toString()}"))
        case _ =>
          Future.failed(new Exception("Unable to delete file!"))
      }
  }

  def upload(host: String, srcPath: String, destPath: String)(implicit ec: ExecutionContext): Future[Unit] = {

    val host0 = getHost(host)
    val request = HttpRequest(HttpMethods.POST, uri = s"http://$host0/${Environment.FILE_SERVER_CONTEXT_PATH}/$destPath")
    val data = HttpEntity(
      ContentTypes.`application/octet-stream`,
      fileStream(srcPath)
    )
    http
      .singleRequest(request.copy(entity = data))
      .flatMap {
        case HttpResponse(StatusCodes.OK, _, entity, _) =>
          entity.dataBytes
            .map(_.utf8String)
            .runForeach(str => log.info(s"Save upload file from: [/$srcPath] to: [$host0/$str]"))
            .map[Unit](_ => Unit)
        case r: HttpResponse =>
          r.discardEntityBytes()
          Future.failed(new Exception(s"Upload request failed, response: ${r.toString()}"))
        case _ =>
          Future.failed(new Exception("Unable to upload file!"))
      }
      .andThen {
        case Failure(err) => log.warning(s"Upload failed: ${err.getMessage}")
        case Success(_) =>
      }
  }

  def download(host: String, srcPath: String, destPath: String)(implicit reqNum: AtomicInteger = new AtomicInteger(0), ec: ExecutionContext): Future[Unit] = {
    if (reqNum.incrementAndGet() > Environment.FILE_SERVER_RECURSIVE_TRANSFER_MAX) {
      throw new IllegalArgumentException("单次下载文件数量超过限制, 最大为" + Environment.FILE_SERVER_RECURSIVE_TRANSFER_MAX)
    }
    val host0 = getHost(host)
    val request = HttpRequest(uri = s"http://$host0/${Environment.FILE_SERVER_CONTEXT_PATH}/$srcPath")
    val path = new File(destPath).getParentFile
    if (!path.exists()) {
      path.mkdirs()
    }

    http
      .singleRequest(request)
      .flatMap[Unit] {
        case HttpResponse(StatusCodes.OK, _, entity, _) =>
          if (entity.contentType == ContentTypes.`application/octet-stream`) {
            entity.dataBytes.runWith(FileIO.toPath(Paths.get(destPath)))
              .map(result => result.status)
              .map {
                case Success(_) =>
                  log.info(s"Download file from: [$host0/$srcPath] to: [/$destPath]")
                case _ =>
              }
          } else if (entity.contentType == ContentTypes.`text/plain(UTF-8)`) {
            entity.dataBytes
              .map(_.utf8String.split(ClusterFileServer.sep))
              .runWith(Sink.collection)
              .map(_.flatten)
              .map(seq => seq.map(sub => download(host, srcPath + "/" + sub, destPath + "/" + sub)))
              .flatMap(seq => seq.reduce((f1, f2) => f1.flatMap(_ => f2)))
          } else {
            entity.discardBytes()
            throw new RuntimeException("unexpected FileServer problem")
          }

        case r: HttpResponse =>
          r.discardEntityBytes()
          Future.failed(new Exception(s"Download request failed, response: ${r.toString()}"))
        case _ =>
          Future.failed(new Exception("Unable to download file!"))
      }
      .andThen {
        case Failure(err) => log.warning(s"Download failed: ${err.getMessage}")
        case Success(_) =>
      }
  }


  private def fileStream(filePath: String, chunkSize: Int = 8192): Source[ByteString,Any] = {
    val file = Paths.get(filePath)
    FileIO.fromPath(file, chunkSize)
      .withAttributes(ActorAttributes.dispatcher("file-io-dispatcher"))
  }

  private def getHost(host: String): String = if (host.split(":").length == 1) host + ":" + Environment.FILE_SERVER_PORT else host
}
