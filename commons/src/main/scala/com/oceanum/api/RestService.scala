package com.oceanum.api

import java.util.Date

import com.oceanum.api.entities.{ClusterNodes, Coordinator, CoordinatorLog, CoordinatorStatus, Elements, NodeTaskInfo, RunWorkflowInfo, WorkflowDefine}
import com.oceanum.common.{FallbackStrategy, GraphMeta, NodeStatus, RerunStrategy}

import scala.concurrent.Future

/**
 * @author chenmingkun
 * @date 2020/8/2
 */
trait RestService {
  def submitWorkflow(workflowDefine: WorkflowDefine): Future[Unit]

  def runWorkflow(name: String, fallbackStrategy: FallbackStrategy, env: Map[String, Any], keepAlive: Boolean, scheduleTime: Option[Date] = None, version: Option[Int]): Future[RunWorkflowInfo]

  def rerunWorkflow(name: String, reRunStrategy: RerunStrategy, env: Map[String, Any], keepAlive: Boolean): Future[RunWorkflowInfo]

  def getWorkflow(name: String): Future[WorkflowDefine]

  def checkWorkflowStatus(name: String): Future[GraphMeta]

  def checkWorkflowStatus(name: String, id: Int): Future[GraphMeta]

  def killWorkflow(name: String, id: Int): Future[Unit]

  def stopWorkflow(name: String): Future[Unit]

  def isWorkflowAlive(name: String): Future[Boolean]

  def submitCoordinator(coordinator: Coordinator): Future[Unit]

  def recover(coordinator: Coordinator): Future[Unit]

  def runCoordinator(name: String): Future[Unit]

  def getCoordinator(name: String): Future[Coordinator]

  def checkCoordinatorStatus(name: String): Future[CoordinatorStatus]

  def stopCoordinator(name: String): Future[Boolean]

  def suspendCoordinator(name: String): Future[Boolean]

  def resumeCoordinator(name: String): Future[Boolean]

  def clusterNodes(status: Option[NodeStatus] = None, host: Option[String] = None, role: Option[String] = None): Future[ClusterNodes]

  def clusterTaskInfos(host: Option[String]): Future[Elements[NodeTaskInfo]]

  def clusterRunningWorkflows(host: Option[String]): Future[Elements[RunWorkflowInfo]]

  def nodeTaskInfo(host: String): Future[NodeTaskInfo]
}
