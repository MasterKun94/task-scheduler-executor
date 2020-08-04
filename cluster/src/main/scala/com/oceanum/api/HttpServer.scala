package com.oceanum.api

import akka.Done
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.oceanum.api.entities.{Coordinator, RunWorkflowInfo, WorkflowDefine}
import com.oceanum.common.{Environment, FallbackStrategy, GraphMeta, Log, RichGraphMeta, SystemInit}

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
 * @author chenmingkun
 * @date 2020/8/2
 */
object HttpServer extends Log {
  private lazy val host = Environment.HOST
  private lazy val port = Environment.FILE_SERVER_PORT
  private implicit lazy val httpMat: ActorMaterializer = ActorMaterializer()
  private lazy val restService = SystemInit.restService
  private lazy val serialization = SystemInit.serialization

  private val route: Route = pathPrefix("api") {
    pathPrefix("workflow"/Segment) { name =>
      (pathPrefix("status") & parameterMap) { map =>
        val future: Future[GraphMeta] = map.get("id") match {
          case Some(id) => restService.checkWorkflowState(name, id.toInt)
          case None => restService.checkWorkflowState(name)
        }
        returnResponseWithEntity(future)
      } ~
        (pathPrefix("run") & parameterMap & extractDataBytes) { (map, bytes) =>
            val future = deserializeAndRun[RichGraphMeta, RunWorkflowInfo](bytes) { meta =>
              restService.runWorkflow(name, meta.fallbackStrategy, meta.env, map.get("keepAlive").forall(_.toBoolean), Option(meta.scheduleTime))
            }
            returnResponseWithEntity(future)
        } ~
        (pathPrefix("rerun") & parameterMap & extractDataBytes) { (map, bytes) =>
            val future = deserializeAndRun[RichGraphMeta, RunWorkflowInfo](bytes) { meta =>
              restService.reRunWorkflow(name, meta.reRunStrategy, meta.env, map.get("keepAlive").forall(_.toBoolean))
            }
            returnResponseWithEntity(future)
        } ~
        (pathPrefix("kill") & parameterMap) { map =>
          val future = restService.killWorkflow(name, map.get("id").map(_.toInt).getOrElse(-1))
          returnResponse(future)
        } ~
        pathPrefix("stop") {
          val future = restService.stopWorkflow(name)
          returnResponse(future)
        } ~
      get {
        val future = restService.getWorkflow(name)
        returnResponseWithEntity(future)
      } ~
        (post & put & extractDataBytes) { bytes =>
          val future = deserializeAndRun[WorkflowDefine, Unit](bytes) { define =>
            restService.submitWorkflow(define.copy(name = name))
          }
          returnResponse(future)
        }

    } ~ {
      pathPrefix("coordinator" / Segment) { name =>
        get {
          val future = restService.getCoordinator(name)
          returnResponseWithEntity(future)
        } ~
          (post & put & extractDataBytes) { bytes =>
            val future = deserializeAndRun[Coordinator, Unit](bytes) { define =>
              restService.submitCoordinator(define.copy(name = name))
            }
              returnResponse(future)
          } ~
          path("run") {
            val future = restService.runCoordinator(name)
            returnResponse(future)
          } ~
          path("status") {
            val future = restService.checkCoordinatorState(name)
            returnResponseWithEntity(future)
          } ~
          path("stop") {
            val future = restService.stopCoordinator(name)
            returnResponse(future)
          } ~
          path("suspend") {
            val future = restService.suspendCoordinator(name)
            returnResponse(future)
          } ~
          (path("resume") & parameterMap) { parameter =>
            val future = restService.resumeCoordinator(name, parameter.get("discard").exists(_.toBoolean))
            returnResponse(future)
          }
      }
    }
  }

  def deserializeAndRun[T<:AnyRef, P](byteString: Source[ByteString, _])(func: T => Future[P])(implicit mf: Manifest[T]): Future[P] = {
    byteString.map(_.utf8String)
      .map(serialization.deSerializeRaw(_)(mf))
      .mapAsync(1)(func)
      .runReduce((f1, _) => f1)
  }

  def returnResponse[T](future: Future[T]): Route = {
    onComplete(future) {
      case Success(_) => complete(Done)
      case Failure(exception) => failWith(exception)
    }
  }

  def returnResponseWithEntity[T<:AnyRef](future: Future[T]): Route = {
    onComplete(future) {
      case Success(t) =>
        complete {
          HttpEntity(
            ContentTypes.`application/json`,
            serialization.serialize(t)
          )
        }
      case Failure(exception) => failWith(exception)
    }
  }

  def start(): Future[Http.ServerBinding] = Http().bindAndHandle(route, host, port)

  def main(args: Array[String]): Unit = {
    Environment.loadEnv(Array("--conf=cluster/src/main/resources/application.properties"))
    Environment.initSystem()
    println(host)
    println(port)
    start()
  }
}
