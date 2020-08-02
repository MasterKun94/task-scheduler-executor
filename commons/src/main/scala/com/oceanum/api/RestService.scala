package com.oceanum.api

import com.oceanum.api.entities.{Coordinator, CoordinatorMetaInfo, WorkflowDefine}
import com.oceanum.common.GraphMeta

import scala.concurrent.Future

/**
 * @author chenmingkun
 * @date 2020/8/2
 */
trait RestService {
  def executeWorkflow(workflowDefine: WorkflowDefine): Future[Unit]

  def getWorkflow(name: String): Future[WorkflowDefine]

  def checkWorkflowState(name: String): Future[GraphMeta]

  def killWorkflow(name: String): Future[Unit]

  def executeCoordinator(coordinator: Coordinator): Future[Unit]

  def getCoordinator(name: String): Future[Coordinator]

  def checkCoordinatorState(name: String): Future[CoordinatorMetaInfo]

  def killCoordinator(name: String): Future[Unit]

  def suspendCoordinator(name: String): Future[Unit]

  def resumeCoordinator(name: String, discardFormerWorkflows: Boolean): Future[Unit]

}
