package com.oceanum.api

import java.util.Date
import java.util.concurrent.atomic.AtomicReference

import com.oceanum.api.entities._
import com.oceanum.common.{FallbackStrategy, GraphMeta, NodeStatus, RerunStrategy}

import scala.concurrent.Future
import scala.util.{Failure, Success}

class ConsistentHashRemoteRestService(seed: String) extends RestService {
  private val hostsRef = new AtomicReference[ClusterNodes]()
  private val remoteRestServices = RemoteRestServices
  import com.oceanum.common.Environment.NONE_BLOCKING_EXECUTION_CONTEXT
  updateRef(seed)

  private def updateRef(host: String): Future[ClusterNodes] = {
    remoteRestServices.get(host).getClusterNodes(status = Some(NodeStatus.UP))
      .andThen {
        case Success(value) => hostsRef.set(value)
        case Failure(exception) => exception.printStackTrace()
      }
  }

  private def execute[T](key: String)(func: RestService => Future[T]): Future[T] = {
    func(remoteRestServices.get(hostsRef.get().consistentHashSelect(key).host))
      .andThen {
        case Failure(_) => updateRef(hostsRef.get().randomSelect().host)
      }
  }

  private def execute[T](func: RestService => Future[T]): Future[T] = {
    func(remoteRestServices.get(hostsRef.get().randomSelect().host))
      .andThen {
        case Failure(_) => updateRef(hostsRef.get().randomSelect().host)
      }
  }

  override def submitWorkflow(workflowDefine: WorkflowDefine): Future[Unit] = {
    execute(workflowDefine.name)(_.submitWorkflow(workflowDefine))
  }

  override def runWorkflow(name: String, fallbackStrategy: FallbackStrategy, env: Map[String, Any], keepAlive: Boolean, scheduleTime: Option[Date] = None, version: Option[Int]): Future[RunWorkflowInfo] = {
    execute(name)(_.runWorkflow(name, fallbackStrategy, env, keepAlive, scheduleTime, version))
  }

  override def rerunWorkflow(name: String, reRunStrategy: RerunStrategy, env: Map[String, Any], keepAlive: Boolean): Future[RunWorkflowInfo] = {
    execute(name)(_.rerunWorkflow(name, reRunStrategy, env, keepAlive))
  }

  override def getWorkflow(name: String): Future[WorkflowDefine] = {
    execute(name)(_.getWorkflow(name))
  }

  override def checkWorkflowStatus(name: String): Future[GraphMeta] = {
    execute(name)(_.checkWorkflowStatus(name))
  }

  override def checkWorkflowStatus(name: String, id: Int): Future[GraphMeta] = {
    execute(name)(_.checkWorkflowStatus(name, id))
  }

  override def killWorkflow(name: String, id: Int): Future[Unit] = {
    execute(name)(_.killWorkflow(name, id))
  }

  override def stopWorkflow(name: String): Future[Unit] = {
    execute(name)(_.stopWorkflow(name))
  }

  override def isWorkflowAlive(name: String): Future[Boolean] = {
    execute(name)(_.isWorkflowAlive(name))
  }

  override def submitCoordinator(coordinator: Coordinator): Future[Unit] = {
    execute(coordinator.name)(_.submitCoordinator(coordinator))
  }

  override def submitAndRunCoordinator(coordinator: Coordinator): Future[Unit] = {
    execute(coordinator.name)(_.submitAndRunCoordinator(coordinator))
  }

  override def runCoordinator(name: String): Future[Unit] = {
    execute(name)(_.runCoordinator(name))
  }

  override def getCoordinator(name: String): Future[Coordinator] = {
    execute(name)(_.getCoordinator(name))
  }

  override def checkCoordinatorStatus(name: String): Future[CoordinatorStatus] = {
    execute(name)(_.checkCoordinatorStatus(name))
  }

  override def stopCoordinator(name: String): Future[Boolean] = {
    execute(name)(_.stopCoordinator(name))
  }

  override def suspendCoordinator(name: String): Future[Boolean] = {
    execute(name)(_.suspendCoordinator(name))
  }

  override def resumeCoordinator(name: String): Future[Boolean] = {
    execute(name)(_.resumeCoordinator(name))
  }

  override def getClusterNodes(status: Option[NodeStatus], host: Option[String], role: Option[String]): Future[ClusterNodes] = {
    execute(_.getClusterNodes(status, host, role))
  }

  override def getClusterTaskInfos(host: Option[String]): Future[Elements[NodeTaskInfo]] = {
    execute(_.getClusterTaskInfos(host))
  }

  override def getNodeTaskInfo(host: String): Future[NodeTaskInfo] = {
    execute(_.getNodeTaskInfo(host))
  }
}
