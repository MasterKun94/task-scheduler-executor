package com.oceanum.api

import com.oceanum.api.entities.{Coordinator, CoordinatorMeta, RunWorkflowInfo, WorkflowDefine}
import com.oceanum.common.{FallbackStrategy, GraphMeta, ReRunStrategy}

import scala.concurrent.Future

/**
 * @author chenmingkun
 * @date 2020/8/2
 */
trait RestService {
  def submitWorkflow(workflowDefine: WorkflowDefine): Future[Unit]

  def runWorkflow(name: String, fallbackStrategy: FallbackStrategy.value, env: Map[String, Any], keepAlive: Boolean): Future[RunWorkflowInfo]

  def reRunWorkflow(name: String, reRunStrategy: ReRunStrategy.value, env: Map[String, Any], keepAlive: Boolean): Future[RunWorkflowInfo]

  def getWorkflow(name: String): Future[WorkflowDefine]

  def checkWorkflowState(name: String): Future[GraphMeta]

  def checkWorkflowState(name: String, id: Int): Future[GraphMeta]

  def killWorkflow(name: String): Future[Unit]

  def stopWorkflow(name: String): Future[Unit]

  def submitCoordinator(coordinator: Coordinator): Future[Unit]

  def runCoordinator(name: String): Future[Unit]

  def getCoordinator(name: String): Future[Coordinator]

  def checkCoordinatorState(name: String): Future[CoordinatorMeta]

  def stopCoordinator(name: String): Future[Unit]

  def suspendCoordinator(name: String): Future[Unit]

  def resumeCoordinator(name: String, discardFormerWorkflows: Boolean): Future[Unit]

}
