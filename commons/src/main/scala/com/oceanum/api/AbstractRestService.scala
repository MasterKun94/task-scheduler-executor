package com.oceanum.api
import com.oceanum.api.entities.{Coordinator, CoordinatorMeta, RunWorkflowInfo, WorkflowDefine}
import com.oceanum.common.{FallbackStrategy, GraphMeta, ReRunStrategy, RichGraphMeta, RichTaskMeta, TaskMeta}
import com.oceanum.persistence.Catalog

import scala.concurrent.Future
import scala.collection.JavaConversions.mapAsJavaMap

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

  override def submitWorkflow(workflowDefine: WorkflowDefine): Future[Unit] = {
    workflowDefineRepo.save(workflowDefine.name, workflowDefine)
  }

  override def runWorkflow(name: String, fallbackStrategy: FallbackStrategy.value, env: Map[String, Any], keepAlive: Boolean): Future[RunWorkflowInfo] = {
    checkWorkflowState(name).flatMap(meta => {
      val newMeta = RichGraphMeta(meta).copy(
        id = meta.id + 1,
        reRunStrategy = ReRunStrategy.NONE,
        env = meta.env ++ env)
      runWorkflow(name, newMeta, keepAlive)
    })
  }

  protected def runWorkflow(name: String, graphMeta: GraphMeta, keepAlive: Boolean): Future[RunWorkflowInfo]

  override def reRunWorkflow(name: String, reRunStrategy: ReRunStrategy.value, env: Map[String, Any], keepAlive: Boolean): Future[RunWorkflowInfo] = {
    if (reRunStrategy == ReRunStrategy.NONE) Future.failed(new IllegalArgumentException("reRunStrategy can not be none"))
    checkWorkflowState(name)
      .map(meta => RichGraphMeta(meta).copy(reRunStrategy = reRunStrategy, env = meta.env ++ env))
      .flatMap(runWorkflow(name, _, keepAlive))
  }

  override def getWorkflow(name: String): Future[WorkflowDefine] = {
    workflowDefineRepo.findById(name).map(_.get)
  }

  override def checkWorkflowState(name: String): Future[GraphMeta] = {
    val env = Map[String, AnyRef]("name" -> name)
    val expr =
      """
        |repo.select(
        | repo.field('name', name),
        | repo.sort('id', 'DESC'),
        | repo.sort('reRunId', 'DESC'),
        | repo.limit(1)
        |)
        |""".stripMargin
    graphMetaRepo.find(expr, env).map { seq =>
      if (seq.isEmpty)
        new RichGraphMeta().copy(name = name, id = -1)
      else
        seq.head
    }
  }

  override def checkWorkflowState(name: String, id: Int): Future[GraphMeta] = {
    val env = Map[String, AnyRef]("name" -> name, "id" -> id.asInstanceOf[AnyRef])
    val expr =
      """
        |repo.select(
        | repo.field('name', name) && repo.field('id', id),
        | repo.sort('reRunId', 'DESC'),
        | repo.limit(1)
        |)
        |""".stripMargin
    graphMetaRepo.find(expr, env).map{ seq =>
      if (seq.isEmpty)
        new RichGraphMeta().copy(name = name, id = id)
      else
        seq.head
    }
  }

  override def submitCoordinator(coordinator: Coordinator): Future[Unit] = {
    coordinatorRepo.save(coordinator.name, coordinator)
  }

  override def getCoordinator(name: String): Future[Coordinator] = {
    coordinatorRepo.findById(name)
      .map(_.get)
      .flatMap(coord =>
        if (coord.workflowDefine.dag == null)
          getWorkflow(coord.workflowDefine.name).map(wf => coord.copy(workflowDefine = wf))
        else
          Future.successful(coord)
      )
  }

  override def checkCoordinatorState(name: String): Future[CoordinatorMeta] = {
    coordinatorMetaRepo.findById(name).map(_.get)
  }
}
