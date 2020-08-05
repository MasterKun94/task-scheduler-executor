package com.oceanum.api

import java.util.Date

import com.oceanum.api.entities.{ClusterNodes, Coordinator, CoordinatorLog, CoordinatorState, RunWorkflowInfo, WorkflowDefine}
import com.oceanum.common.{FallbackStrategy, GraphMeta, ReRunStrategy}

import scala.concurrent.Future

/**
 * @author chenmingkun
 * @date 2020/8/2
 */
trait RestService {
  def submitWorkflow(workflowDefine: WorkflowDefine): Future[Unit]

  def runWorkflow(name: String, fallbackStrategy: FallbackStrategy.value, env: Map[String, Any], keepAlive: Boolean, scheduleTime: Option[Date] = None, version: Option[Int]): Future[RunWorkflowInfo]

  def reRunWorkflow(name: String, reRunStrategy: ReRunStrategy.value, env: Map[String, Any], keepAlive: Boolean): Future[RunWorkflowInfo]

  def getWorkflow(name: String): Future[WorkflowDefine]

  def checkWorkflowState(name: String): Future[GraphMeta]

  def checkWorkflowState(name: String, id: Int): Future[GraphMeta]

  def killWorkflow(name: String, id: Int): Future[Unit]

  def stopWorkflow(name: String): Future[Unit]

  def isWorkflowAlive(name: String): Future[Boolean]

  def submitCoordinator(coordinator: Coordinator): Future[Unit]

  def runCoordinator(name: String): Future[Unit]

  def getCoordinator(name: String): Future[Coordinator]

  def checkCoordinatorState(name: String): Future[CoordinatorState]

  def stopCoordinator(name: String): Future[Boolean]

  def suspendCoordinator(name: String): Future[Boolean]

  def resumeCoordinator(name: String, discardFormerWorkflows: Boolean): Future[Boolean]

  def getClusterNodes(status: Option[String], host: Option[String], role: Option[String]): Future[ClusterNodes]
}
