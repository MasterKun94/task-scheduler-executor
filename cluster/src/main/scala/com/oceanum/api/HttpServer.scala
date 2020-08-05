package com.oceanum.api

import akka.Done
import akka.cluster.{Cluster, MemberStatus}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.oceanum.api.entities.{ClusterNode, ClusterNodes, Coordinator, RunWorkflowInfo, WorkflowDefine}
import com.oceanum.common._
import com.oceanum.common.Environment.NONE_BLOCKING_EXECUTION_CONTEXT

import scala.concurrent.Future
import scala.util.{Failure, Success}

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
  private val cluster = Cluster(system)

  private val route: Route = pathPrefix("api") {
    pathPrefix("workflow"/Segment) { name =>
      pathPrefix("status") {
        checkWorkflowStatus(name)   //api/workflow/{name}/status
      } ~
        pathPrefix("run") {
          runWorkflow(name)   //api/workflow/{name}/run
        } ~
        pathPrefix("rerun") {
          reRunWorkflow(name)   //api/workflow/{name}/rerun
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
        (post & put) {
          submitCoordinator(name)   //api/coordinator/{name}
        }
    } ~
    pathPrefix("cluster") {
      path("nodes") {
        clusterNodes()    //cluster/nodes
      }
    }
  }

  def checkWorkflowStatus(name: String): Route = {
    parameterMap { map =>
      val future: Future[GraphMeta] = map.get("id") match {
        case Some(id) => restService.checkWorkflowState(name, id.toInt)
        case None => restService.checkWorkflowState(name)
      }
      returnResponseWithEntity(future)
    }
  }

  def runWorkflow(name: String): Route = {
    (parameterMap & extractRequestEntity) { (map, entity) =>
      val future = deserializeAndRun[RichGraphMeta, RunWorkflowInfo](entity) { meta =>
        restService.runWorkflow(name, meta.fallbackStrategy, meta.env, map.get("keepAlive").forall(_.toBoolean), Option(meta.scheduleTime), map.get("version").map(_.toInt))
      }
      returnResponseWithEntity(future)
    }
  }

  def reRunWorkflow(name: String): Route = {
    (parameterMap & extractRequestEntity) { (map, entity) =>
      val future = deserializeAndRun[RichGraphMeta, RunWorkflowInfo](entity) { meta =>
        restService.reRunWorkflow(name, meta.reRunStrategy, meta.env, map.get("keepAlive").forall(_.toBoolean))
      }
      returnResponseWithEntity(future)
    }
  }

  def killWorkflow(name: String): Route = {
    parameterMap { map =>
      val future = restService.killWorkflow(name, map.get("id").map(_.toInt).getOrElse(-1))
      returnResponse(future)
    }
  }

  def stopWorkflow(name: String): Route = {
    val future = restService.stopWorkflow(name)
    returnResponse(future)
  }

  def getWorkflow(name: String): Route = {
    val future = restService.getWorkflow(name)
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
    val future = restService.runCoordinator(name)
    returnResponse(future)
  }

  def checkCoordinatorStatus(name: String): Route = {
    val future = restService.checkCoordinatorState(name)
    returnResponseWithEntity(future)
  }

  def stopCoordinator(name: String): Route = {
    val future = restService.stopCoordinator(name)
    returnResponse(future)
  }

  def suspendCoordinator(name: String): Route = {
    val future = restService.suspendCoordinator(name)
    returnResponse(future)
  }

  def resumeCoordinator(name: String): Route = {
    parameterMap { parameter =>
      val future = restService.resumeCoordinator(name, parameter.get("discard").exists(_.toBoolean))
      returnResponse(future)
    }
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
      val nodes = restService.getClusterNodes(map.get("status"), map.get("host"), map.get("role"))
      returnResponseWithEntity(nodes)
    }
  }

  def deserializeAndRun[T<:AnyRef, P](entity: RequestEntity)(func: T => Future[P])(implicit mf: Manifest[T]): Future[P] = {
    Unmarshal(entity).to[String]
      .map(serialization.deSerializeRaw(_)(mf))
      .flatMap(func)
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
