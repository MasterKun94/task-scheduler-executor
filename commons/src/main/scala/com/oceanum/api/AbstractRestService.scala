package com.oceanum.api
import java.util.Date
import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import com.oceanum.api.entities.{Coordinator, CoordinatorLog, CoordinatorState, RunWorkflowInfo, WorkflowDefine}
import com.oceanum.common._
import com.oceanum.persistence.Catalog
import com.oceanum.triger.Triggers

import scala.collection.JavaConversions.mapAsJavaMap
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success, Try}

/**
 * @author chenmingkun
 * @date 2020/8/2
 */
abstract class AbstractRestService extends RestService {
  private val workflowDefineRepo = Catalog.getRepository[WorkflowDefine]
  private val coordinatorRepo = Catalog.getRepository[Coordinator]
  private val graphMetaRepo = Catalog.getRepository[GraphMeta]
  private val coordinatorLogRepo = Catalog.getRepository[CoordinatorLog]
  private val coordinatorStateRepo = Catalog.getRepository[CoordinatorState]
  import com.oceanum.common.Environment.NONE_BLOCKING_EXECUTION_CONTEXT

  def actorSystem: ActorSystem

  override def submitWorkflow(workflowDefine: WorkflowDefine): Future[Unit] = {
    workflowDefineRepo.save(workflowDefine.name, workflowDefine)
  }

  override def runWorkflow(name: String, fallbackStrategy: FallbackStrategy.value, env: Map[String, Any], keepAlive: Boolean, scheduleTime: Option[Date]): Future[RunWorkflowInfo] = {
    checkWorkflowState(name).flatMap(meta => {
      val newMeta = RichGraphMeta(meta).copy(
        id = meta.id + 1,
        reRunStrategy = ReRunStrategy.NONE,
        env = meta.env ++ env,
        createTime = new Date(),
        scheduleTime = scheduleTime.getOrElse(new Date()),
        startTime = null,
        endTime = null,
        reRunId = 0,
        reRunFlag = false,
        host = Environment.HOST)
      runWorkflow(name, newMeta, keepAlive)
    })
  }

  protected def runWorkflow(name: String, graphMeta: GraphMeta, keepAlive: Boolean): Future[RunWorkflowInfo]

  override def reRunWorkflow(name: String, reRunStrategy: ReRunStrategy.value, env: Map[String, Any], keepAlive: Boolean): Future[RunWorkflowInfo] = {
    if (reRunStrategy == ReRunStrategy.NONE) Future.failed(new IllegalArgumentException("reRunStrategy can not be none"))
    checkWorkflowState(name)
      .map(meta => RichGraphMeta(meta).copy(
        reRunStrategy = reRunStrategy,
        env = meta.env ++ env,
        startTime = null,
        endTime = null,
        reRunFlag = false,
        host = Environment.HOST
      ))
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
    coordinatorRepo.save(coordinator.name, coordinator).flatMap { _ =>
      submitWorkflow(coordinator.workflowDefine)
    }
  }

  override def runCoordinator(name: String): Future[Unit] = {
    getCoordinator(name).flatMap[Unit] { coord =>
      val trigger = Triggers.getTrigger(coord.trigger.name)
      trigger
        .start(name, coord.trigger.config) { date =>
          runWorkflow(name, coord.fallbackStrategy, coord.workflowDefine.env, keepAlive = true, scheduleTime = Some(date))
            .onComplete {
              updateCoordinatorLog(coord, _)
            }
        }
      coord.endTime match {
        case Some(date) =>
          val duration = FiniteDuration(date.getTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
          Scheduler.scheduleOnce(duration) {
            stopCoordinator(name)
          }(actorSystem)
        case None => // do nothing
      }
      coordinatorStateRepo.save(name, CoordinatorState(name, CoordinatorState.RUNNING))
    }
  }

  def updateCoordinatorLog(coordinator: Coordinator, workflowInfo: Try[RunWorkflowInfo]): Future[Unit] = {
    val log = workflowInfo match {
      case Success(value) =>
        CoordinatorLog(
          name = coordinator.name,
          workflowName = coordinator.workflowDefine.name,
          workflowId = Some(value.id),
          workflowSubmitted = true,
          timestamp = new Date(),
          error = None
        )
      case Failure(exception) =>
        CoordinatorLog(
          name = coordinator.name,
          workflowName = coordinator.workflowDefine.name,
          workflowId = None,
          workflowSubmitted = false,
          timestamp = new Date(),
          error = Some(exception)
        )
    }
    coordinatorLogRepo.save(log).map(_ => Unit)
  }

  override def suspendCoordinator(name: String): Future[Boolean] = {
    getCoordinator(name)
      .flatMap { coord =>
        if (Triggers.getTrigger(coord.trigger.name).suspend(name)) {
          coordinatorStateRepo.save(name, CoordinatorState(name, CoordinatorState.SUSPENDED)).map(_ => true)
        } else {
          Future(false)
        }
      }
  }

  override def stopCoordinator(name: String): Future[Boolean] = {
    getCoordinator(name).flatMap { coord =>
      stopWorkflow(coord.workflowDefine.name)
        .flatMap { _ =>
          if (Triggers.getTrigger(coord.trigger.name).stop(name)) {
            coordinatorStateRepo.save(name, CoordinatorState(name, CoordinatorState.STOPPED)).map(_ => true)
          } else {
            Future(false)
          }
        }
    }
  }

  override def resumeCoordinator(name: String, discardFormerWorkflows: Boolean): Future[Boolean] = {
    getCoordinator(name)
      .flatMap { coord =>
        if (Triggers.getTrigger(coord.trigger.name).resume(name)) {
          coordinatorStateRepo.save(name, CoordinatorState(name, CoordinatorState.RUNNING)).map(_ => true)
        } else {
          Future(false)
        }
      }
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

  override def checkCoordinatorState(name: String): Future[CoordinatorState] = {
    coordinatorStateRepo.findById(name).map(_.get)
  }
}
