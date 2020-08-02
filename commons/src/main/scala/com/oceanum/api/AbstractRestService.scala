package com.oceanum.api
import com.oceanum.api.entities.{Coordinator, CoordinatorMeta, WorkflowDefine}
import com.oceanum.common.{GraphMeta, TaskMeta}
import com.oceanum.persistence.Catalog

import scala.concurrent.Future

/**
 * @author chenmingkun
 * @date 2020/8/2
 */
abstract class AbstractRestService extends RestService {
  private val workflowDefineRepo = Catalog.getRepository[WorkflowDefine]
  private val coordinatorRepo = Catalog.getRepository[Coordinator]
  private val graphMetaRepo = Catalog.getRepository[GraphMeta]
  private val coordinatorMetaRepo = Catalog.getRepository[CoordinatorMeta]
  import com.oceanum.common.Environment.NONE_BLOCKING_EXECUTION_CONTEXT

  override def getWorkflow(name: String): Future[WorkflowDefine] = workflowDefineRepo.findById(name).map(_.get)

  override def checkWorkflowState(name: String): Future[GraphMeta] = ???

  override def getCoordinator(name: String): Future[Coordinator] = coordinatorRepo.findById(name).map(_.get)

  override def checkCoordinatorState(name: String): Future[CoordinatorMeta] = coordinatorMetaRepo.findById(name).map(_.get)
}
