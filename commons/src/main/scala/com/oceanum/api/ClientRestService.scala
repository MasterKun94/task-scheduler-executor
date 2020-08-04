package com.oceanum.api

import java.util.Date

import com.oceanum.annotation.IRestService
import com.oceanum.api.entities.{Coordinator, CoordinatorState, RunWorkflowInfo, WorkflowDefine}
import com.oceanum.common.{FallbackStrategy, GraphMeta, ReRunStrategy}

import scala.concurrent.Future

@IRestService
class ClientRestService extends RestService {
  override def submitWorkflow(workflowDefine: WorkflowDefine): Future[Unit] = ???

  override def runWorkflow(name: String, fallbackStrategy: FallbackStrategy.value, env: Map[String, Any], keepAlive: Boolean, scheduleTime: Option[Date] = None): Future[RunWorkflowInfo] = ???

  override def reRunWorkflow(name: String, reRunStrategy: ReRunStrategy.value, env: Map[String, Any], keepAlive: Boolean): Future[RunWorkflowInfo] = ???

  override def getWorkflow(name: String): Future[WorkflowDefine] = ???

  override def checkWorkflowState(name: String): Future[GraphMeta] = ???

  override def checkWorkflowState(name: String, id: Int): Future[GraphMeta] = ???

  override def killWorkflow(name: String, id: Int): Future[Unit] = ???

  override def stopWorkflow(name: String): Future[Unit] = ???

  override def isWorkflowAlive(name: String): Future[Boolean] = ???

  override def submitCoordinator(coordinator: Coordinator): Future[Unit] = ???

  override def runCoordinator(name: String): Future[Unit] = ???

  override def getCoordinator(name: String): Future[Coordinator] = ???

  override def checkCoordinatorState(name: String): Future[CoordinatorState] = ???

  override def stopCoordinator(name: String): Future[Boolean] = ???

  override def suspendCoordinator(name: String): Future[Boolean] = ???

  override def resumeCoordinator(name: String, discardFormerWorkflows: Boolean): Future[Boolean] = ???
}
