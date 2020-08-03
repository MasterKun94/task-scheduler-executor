package com.oceanum.api

import akka.Done
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.oceanum.api.entities.{Coordinator, WorkflowDefine}
import com.oceanum.common.{Environment, Log, SystemInit}

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
 * @author chenmingkun
 * @date 2020/8/2
 */
object HttpServer extends Log {
  private val host = Environment.HOST
  private val port = Environment.FILE_SERVER_PORT
  private implicit lazy val httpMat: ActorMaterializer = ActorMaterializer()
  private lazy val restService = SystemInit.restService
  private lazy val serialization = SystemInit.serialization

  private val route: Route = pathPrefix("api") {
    pathPrefix("workflow" / Segment) { name =>
      get {
        val future = restService.getWorkflow(name)
        returnResponseWithEntity(future)
      } ~
        path("execute") {
          extractDataBytes { bytes =>
            val future = bytes
              .map(_.utf8String)
              .map(serialization.deSerializeRaw[WorkflowDefine])
              .map(_.copy(name = name))
              .mapAsync(1)(restService.submitWorkflow)
              .runForeach(unit => unit)
            returnResponse(future)
          }
        } ~
        path("status") {
          val future = restService.checkWorkflowState(name)
          returnResponseWithEntity(future)
        } ~
        path("kill") {
          val future = restService.killWorkflow(name)
          returnResponse(future)
        }

    } ~ {
      pathPrefix("coordinator" / Segment) { name =>
        get {
          val future = restService.getCoordinator(name)
          returnResponseWithEntity(future)
        } ~
          path("execute") {
            extractDataBytes { bytes =>
              val future = bytes
                .map(_.utf8String)
                .map(serialization.deSerializeRaw[Coordinator])
                .map(_.copy(name = name))
                .mapAsync(1)(restService.submitCoordinator)
                .runForeach(unit => unit)
              returnResponse(future)
            }
          } ~
          path("status") {
            val future = restService.checkCoordinatorState(name)
            returnResponseWithEntity(future)
          } ~
          path("kill") {
            val future = restService.killCoordinator(name)
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
    Environment.loadEnv(args)
    println(host)
    println(port)
    start()
  }
}
