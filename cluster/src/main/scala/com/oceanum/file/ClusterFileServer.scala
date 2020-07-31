package com.oceanum.file

import java.io.File
import java.nio.file._
import java.util.concurrent.atomic.AtomicInteger
import java.util.regex.Matcher

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream._
import akka.stream.scaladsl._
import akka.util.ByteString
import com.oceanum.common.{Environment, Log, ActorSystems}

import scala.annotation.tailrec
import scala.concurrent.Future

/**
 * @author chenmingkun
 * @date 2020/6/25
 */
object ClusterFileServer extends Log(ActorSystems.FILE_SERVER_SYSTEM) {
  private val host = Environment.HOST
  private val port = Environment.FILE_SERVER_PORT
  private val chunkSize = Environment.FILE_SERVER_CHUNK_SIZE
  private val basePath = Environment.FILE_SERVER_BASE_PATH
  private implicit lazy val httpMat: ActorMaterializer = ActorMaterializer()
  val sep = ",\r\n"

  private def fileStream(filePath: String): Source[ByteString, Future[IOResult]] = {
    val file: Path = Paths.get(filePath)
    FileIO.fromPath(file, chunkSize)
      .withAttributes(ActorAttributes.dispatcher("file-io-dispatcher"))
  }

  private def getPath(fp: String): String = {
    (basePath + parser(fp)).replaceAll("%5C", Matcher.quoteReplacement(Environment.PATH_SEPARATOR))
  }

  @tailrec
  private def parser(path: String): String = {
    if (path.startsWith("/")) parser(path.substring(1)) else path
  }

  private val route: Route = pathPrefix(Environment.FILE_SERVER_CONTEXT_PATH) {
    (get & path(Remaining)) { fp =>
      val path = getPath(fp)
      val file = new File(path)
      if (file.exists() && file.isDirectory) {
        complete(
          HttpEntity(
            ContentTypes.`text/plain(UTF-8)`,
            file.list().mkString(sep)
          )
        )
      } else if (file.isFile) {
        withoutSizeLimit {
          complete(
            HttpEntity(
              ContentTypes.`application/octet-stream`,
              fileStream(path))
          )
        }
      } else {
        failWith(new IllegalArgumentException(s"文件不存在：$path"))
      }
    } ~
      (post & path(Remaining)) { fp =>
        val path = getPath(fp)
        withoutSizeLimit {
          extractDataBytes { bytes =>
            val file = new File(path).getParentFile
            if (!file.exists()) {
              file.mkdirs();
            }
            val fut = bytes.runWith(FileIO.toPath(Paths.get(path)))
            onComplete(fut) { _ =>
              complete(path)
            }
          }
        }
      } ~
      (put & path(Remaining)) { fp =>
        val path = getPath(fp)
        extractDataBytes { bytes =>
          val fut = bytes
            .map(_.utf8String)
            .map(str => str.split(sep)
              .map(_.split("="))
              .map(arr => (arr(0), arr(1)))
              .toMap
            )
            .runWith(Sink.foreachAsync(1) { map =>
              ClusterFileServerApi.download(map("host"), map("path"), path)(new AtomicInteger(0), httpMat.system.dispatcher)
            })
          onComplete(fut) { _ =>
            complete(path)
          }
        }
      } ~
      (delete & path(Remaining)) { fp =>
        val path = getPath(fp)
        extractRequestEntity { e =>
          e.discardBytes()
          val file = new File(path)
          val flag = file.delete()
          if (flag) {
            complete(fp)
          } else {
            failWith(new Exception("delete failed, path: " + file))
          }
        }
      }
  }

  def start(): Future[Http.ServerBinding] = Http().bindAndHandle(route, host, port)

  def main(args: Array[String]): Unit = {
    Environment.loadEnv(args)
    Environment.print()
    start()
  }
}
