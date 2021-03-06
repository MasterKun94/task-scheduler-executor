package com.oceanum.api

import java.util.Date

import com.oceanum.api.entities._
import com.oceanum.common._

import scala.concurrent.Future

class RemoteRestService(host: String) extends RestService {
  private def hostPort: String = "http://" + host + ":" + Environment.REST_SERVER_PORT
  import Environment.NONE_BLOCKING_EXECUTION_CONTEXT

  override def submitWorkflow(workflowDefine: WorkflowDefine): Future[Unit] = {
    HttpClient.post[WorkflowDefine, Nothing](
      url = hostPort + "/api/workflow/" + workflowDefine.name,
      entity = Some(workflowDefine)
    )
  }

  override def runWorkflow(name: String, fallbackStrategy: FallbackStrategy, env: Map[String, Any] = Map.empty, keepAlive: Boolean = false, scheduleTime: Option[Date] = None, version: Option[Int]): Future[RunWorkflowInfo] = {
    val graphMeta = new RichGraphMeta(
      name = name,
      fallbackStrategy = fallbackStrategy,
      env = env,
      scheduleTime = scheduleTime.orElse(Option(new Date()))
    )
    HttpClient.post[GraphMeta, RunWorkflowInfo](
      url = hostPort + "/api/workflow/" + name + "/run",
      entity = Some(graphMeta),
      param = Map("keepAlive" -> keepAlive.toString) ++ version.map(v => "version" -> v.toString)
    )
  }

  override def rerunWorkflow(name: String, reRunStrategy: RerunStrategy, env: Map[String, Any] = Map.empty, keepAlive: Boolean = false): Future[RunWorkflowInfo] = {
    val graphMeta = new RichGraphMeta(
      name = name,
      rerunStrategy = reRunStrategy,
      env = env
    )
    HttpClient.post[GraphMeta, RunWorkflowInfo](
      url = hostPort + "/api/workflow/" + name + "/rerun",
      entity = Some(graphMeta),
      param = Map("keepAlive" -> keepAlive.toString)
    )
  }

  override def getWorkflow(name: String): Future[WorkflowDefine] = {
    HttpClient.get[Nothing, WorkflowDefine](
      url = hostPort + "/api/workflow/" + name
    )
  }

  override def checkWorkflowStatus(name: String): Future[GraphMeta] = {
    HttpClient.get[Nothing, GraphMeta](
      url = hostPort + "/api/workflow/" + name + "/status"
    )
  }

  override def checkWorkflowStatus(name: String, id: Int): Future[GraphMeta] = {
    HttpClient.get[Nothing, GraphMeta](
      url = hostPort + "/api/workflow/" + name + "/status",
      param = Map("id" -> id.toString)
    )
  }

  override def killWorkflow(name: String, id: Int): Future[Unit] = {
    HttpClient.post[Nothing, Nothing](
      url = hostPort + "/api/workflow/" + name + "/kill",
      param = Map("id" -> id.toString)
    )
  }

  override def stopWorkflow(name: String): Future[Unit] = {
    HttpClient.post[Nothing, Nothing](
      url = hostPort + "/api/workflow/" + name + "/stop"
    )
  }

  override def isWorkflowAlive(name: String): Future[Boolean] = {
    getWorkflow(name).map(_.alive)
  }

  override def submitCoordinator(coordinator: Coordinator): Future[Unit] = {
    HttpClient.post[Coordinator, Nothing](
      url = hostPort + "/api/coordinator/" + coordinator.name,
      entity = Some(coordinator)
    )
  }

  override def recover(coordinator: Coordinator): Future[Unit] = {
    HttpClient.post[Coordinator, Nothing](
      url = hostPort + "/api/coordinator/" + coordinator.name + "/recover",
      entity = Some(coordinator)
    )
  }

  override def runCoordinator(name: String): Future[Unit] = {
    HttpClient.post[Nothing, Nothing](
      url = hostPort + "/api/coordinator/" + name + "/run"
    )
  }

  override def getCoordinator(name: String): Future[Coordinator] = {
    HttpClient.get[Nothing, Coordinator](
      url = hostPort + "/api/coordinator/" + name
    )
  }

  override def checkCoordinatorStatus(name: String): Future[CoordinatorStatus] = {
    HttpClient.get[Nothing, CoordinatorStatus](
      url = hostPort + "/api/coordinator/" + name + "/status"
    )
  }

  override def stopCoordinator(name: String): Future[Boolean] = {
    HttpClient.post[Nothing, BoolValue](
      url = hostPort + "/api/coordinator/" + name + "/stop"
    ).map(_.value)
  }

  override def suspendCoordinator(name: String): Future[Boolean] = {
    HttpClient.post[Nothing, BoolValue](
      url = hostPort + "/api/coordinator/" + name + "/suspend"
    ).map(_.value)
  }

  override def resumeCoordinator(name: String): Future[Boolean] = {
    HttpClient.post[Nothing, BoolValue](
      url = hostPort + "/api/coordinator/" + name + "/resume"
    ).map(_.value)
  }

  override def clusterNodes(status: Option[NodeStatus], host: Option[String], role: Option[String]): Future[ClusterNodes] = {
    HttpClient.get[Nothing, ClusterNodes](
      url = hostPort + "/api/cluster/nodes",
      param = Map(
        "status" -> status.map(_.toString),
        "host" -> host,
        "role" -> role
      )
        .filter(_._2.isDefined)
        .mapValues(_.get)
    )
  }

  override def clusterTaskInfos(host: Option[String]): Future[Page[NodeTaskInfo]] = {
    HttpClient.get[Nothing, Page[NodeTaskInfo]](
      url = hostPort + "/api/cluster/task-infos",
      param = host.map(s => "host" -> s).toMap
    )
  }

  override def nodeTaskInfo(host: String): Future[NodeTaskInfo] = {
    HttpClient.get[Nothing, NodeTaskInfo](
      url = hostPort + "/api/node/task-info",
      param = Map("host" -> host)
    )
  }

  override def searchWorkflows(searchRequest: SearchRequest): Future[Page[GraphMeta]] = {
    HttpClient.post[SearchRequest, Page[GraphMeta]](
      url = hostPort + "/api/search/workflow",
      entity = Some(searchRequest)
    )
  }

  override def searchCoordinators(searchRequest: SearchRequest): Future[Page[CoordinatorStatus]] = {
    HttpClient.post[SearchRequest, Page[CoordinatorStatus]](
      url = hostPort + "/api/search/coordinator",
      entity = Some(searchRequest)
    )
  }
}
