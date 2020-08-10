package com.oceanum.api

import akka.Done
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Route, StandardRoute}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.oceanum.api.entities._
import com.oceanum.common.Environment.NONE_BLOCKING_EXECUTION_CONTEXT
import com.oceanum.common._

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
 * @author chenmingkun
 * @date 2020/8/2
 */
object HttpServer extends Log {
  private lazy val host = Environment.HOST
  private lazy val port = Environment.REST_SERVER_PORT
  private implicit lazy val httpMat: ActorMaterializer = ActorMaterializer()
  private lazy val restService = SystemInit.restService
  private lazy val serialization = SystemInit.serialization

  private val route: Route = pathPrefix("api") {
    pathPrefix("workflow"/Segment) { name =>
      pathPrefix("status") {
        checkWorkflowStatus(name)   //api/workflow/{name}/status
      } ~
        pathPrefix("run") {
          runWorkflow(name)   //api/workflow/{name}/run
        } ~
        pathPrefix("rerun") {
          rerunWorkflow(name)   //api/workflow/{name}/rerun
        } ~
        pathPrefix("kill") {
          killWorkflow(name)   //api/workflow/{name}/kill
        } ~
        pathPrefix("stop") {
          stopWorkflow(name)   //api/workflow/{name}/stop
        } ~
        get {
          getWorkflow(name)   //api/workflow/{name}
        } ~
        (post | put) {
          submitWorkflow(name)   //api/workflow/{name}
        }
    } ~
    pathPrefix("coordinator"/Segment) { name =>
      path("run") {
        runCoordinator(name)   //api/coordinator/{name}/run
      } ~
        path("status") {
          checkCoordinatorStatus(name)   //api/coordinator/{name}/status
        } ~
        path("stop") {
          stopCoordinator(name)   //api/coordinator/{name}/stop
        } ~
        path("suspend") {
          suspendCoordinator(name)   //api/coordinator/{name}/suspend
        } ~
        path("resume") {
          resumeCoordinator(name)   //api/coordinator/{name}/resume
        } ~
        get {
          getCoordinator(name)   //api/coordinator/{name}
        } ~
        (post | put) {
          submitCoordinator(name)   //api/coordinator/{name}
        }
    } ~
    pathPrefix("cluster") {
      path("nodes") {
        clusterNodes()    //cluster/nodes
      } ~
      path("task-infos") {
        clusterTaskInfo()
      }
    } ~
    pathPrefix("node") {
      path("task-info") {
        taskInfo()
      }
    }
  }

  def checkWorkflowStatus(name: String): Route = {
    parameterMap { map =>
      val future: Future[GraphMeta] = map.get("id") match {
        case Some(id) => restService.checkWorkflowStatus(name, id.toInt)
        case None => restService.checkWorkflowStatus(name)
      }
      returnResponseWithEntity(future)
    }
  }

  def runWorkflow(name: String): Route = {
    (parameterMap & extractRequestEntity) { (map, entity) =>
      val future: Future[RunWorkflowInfo] = deserializeAndRun[RichGraphMeta, RunWorkflowInfo](entity) { meta =>
        restService.runWorkflow(name, meta.fallbackStrategy, meta.env, map.get("keepAlive").forall(_.toBoolean), meta.scheduleTime, map.get("version").map(_.toInt))
      }
      returnResponseWithEntity(future)
    }
  }

  def rerunWorkflow(name: String): Route = {
    (parameterMap & extractRequestEntity) { (map, entity) =>
      val future: Future[RunWorkflowInfo] = deserializeAndRun[RichGraphMeta, RunWorkflowInfo](entity) { meta =>
        restService.rerunWorkflow(name, meta.rerunStrategy, meta.env, map.get("keepAlive").forall(_.toBoolean))
      }
      returnResponseWithEntity(future)
    }
  }

  def killWorkflow(name: String): Route = {
    parameterMap { map =>
      val future: Future[Unit] = restService.killWorkflow(name, map.get("id").map(_.toInt).getOrElse(-1))
      returnResponse(future)
    }
  }

  def stopWorkflow(name: String): Route = {
    val future: Future[Unit] = restService.stopWorkflow(name)
    returnResponse(future)
  }

  def getWorkflow(name: String): Route = {
    val future: Future[WorkflowDefine] = restService.getWorkflow(name)
    returnResponseWithEntity(future)
  }

  def submitWorkflow(name: String): Route = {
    extractRequestEntity { entity =>
      val future = deserializeAndRun[WorkflowDefine, Unit](entity) { define =>
        restService.submitWorkflow(define.copy(name = name))
      }
      returnResponse(future)
    }
  }

  def runCoordinator(name: String): Route = {
    extractRequestEntity { entity =>
      val future: Future[Unit] = if (entity.isKnownEmpty()) {
        restService.runCoordinator(name)
      } else {
        deserializeAndRun[Coordinator, Unit](entity) { coord =>
          restService.submitAndRunCoordinator(coord.copy(name = name))
        }
      }
      returnResponse(future)
    }
  }

  def checkCoordinatorStatus(name: String): Route = {
    val future: Future[CoordinatorStatus] = restService.checkCoordinatorStatus(name)
    returnResponseWithEntity(future)
  }

  def stopCoordinator(name: String): Route = {
    val future: Future[Boolean] = restService.stopCoordinator(name)
    returnResponseWithEntity(future.map(BoolValue))
  }

  def suspendCoordinator(name: String): Route = {
    val future: Future[Boolean] = restService.suspendCoordinator(name)
    returnResponseWithEntity(future.map(BoolValue))
  }

  def resumeCoordinator(name: String): Route = {
    val future: Future[Boolean] = restService.resumeCoordinator(name)
    returnResponseWithEntity(future.map(BoolValue))
  }

  def submitCoordinator(name: String): Route = {
    extractRequestEntity { entity =>
      val future = deserializeAndRun[Coordinator, Unit](entity) { define =>
        restService.submitCoordinator(define.copy(name = name))
      }
      returnResponse(future)
    }
  }

  def getCoordinator(name: String): Route = {
    val future = restService.getCoordinator(name)
    returnResponseWithEntity(future)
  }

  def clusterNodes(): Route = {
    parameterMap { map =>
      val nodes = restService.getClusterNodes(map.get("status").map(NodeStatus.valueOf), map.get("host"), map.get("role"))
      returnResponseWithEntity(nodes)
    }
  }

  def taskInfo(): Route = {
    parameterMap { map =>
      val taskInfo = restService.getNodeTaskInfo(map.getOrElse("host", Environment.HOST))
      returnResponseWithEntity(taskInfo)
    }
  }

  def clusterTaskInfo(): Route = {
    parameterMap { map =>
      val future = restService.getClusterTaskInfos(map.get("host"))
      returnResponseWithEntity(future)
    }
  }

  def deserializeAndRun[T<:AnyRef, P](entity: RequestEntity)(func: T => Future[P])(implicit mf: Manifest[T]): Future[P] = {
    Unmarshal(entity).to[String]
      .map(serialization.deSerializeRaw(_)(mf))
      .flatMap(func)
  }

  def returnResponse[T](future: Future[T]): Route = {
    onComplete(future)(completeResponse(withEntity = false))
  }

  def returnResponseWithEntity[T<:AnyRef](future: Future[T]): Route = {
    onComplete(future)(completeResponse(withEntity = true))
  }

  def completeResponse[T](withEntity: Boolean)(obj: Try[T]): StandardRoute = {
    obj match {
      case Success(value) =>
        if (withEntity) {
          complete {
            HttpEntity(
              ContentTypes.`application/json`,
              serialization.serialize(value.asInstanceOf[AnyRef])
            )
          }
        } else {
          complete(Done)
        }

      case Failure(exception) =>
        val statusCode = exception match {
          case _: RuntimeException => StatusCodes.InternalServerError
          case _ => StatusCodes.BadRequest
        }
        complete {
          HttpResponse(
            status = statusCode,
            entity = HttpEntity(
              ContentTypes.`application/json`,
              serialization.serialize(exception)
            ))
        }
    }
  }

  def start(): Future[Http.ServerBinding] = Http().bindAndHandle(route, host, port).andThen {
    case Failure(e) => e.printStackTrace()
  }
}
