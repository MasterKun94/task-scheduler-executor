package com.oceanum.api

import java.util.Date
import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Cancellable}
import com.oceanum.api.entities._
import com.oceanum.common._
import com.oceanum.exceptions.{BadRequestException, VersionOutdatedException}
import com.oceanum.expr.{Evaluator, JavaHashMap, JavaMap}
import com.oceanum.persistence.Catalog
import com.oceanum.trigger.Triggers

import scala.collection.JavaConversions.mapAsJavaMap
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success, Try}

/**
 * @author chenmingkun
 */
abstract class AbstractRestService extends Log with RestService {
  private val workflowDefineRepo = Catalog.getRepository[WorkflowDefine]
  private val coordinatorRepo = Catalog.getRepository[Coordinator]
  private val graphMetaRepo = Catalog.getRepository[GraphMeta]
  private val coordinatorLogRepo = Catalog.getRepository[CoordinatorLog]
  private val coordinatorStateRepo = Catalog.getRepository[CoordinatorStatus]
  import com.oceanum.common.Environment.NONE_BLOCKING_EXECUTION_CONTEXT

  def actorSystem: ActorSystem

  private def isLocal(host: Option[String]): Boolean = host.forall(_.equals(Environment.HOST))

  override def submitWorkflow(workflowDefine: WorkflowDefine): Future[Unit] = {
    workflowDefine.host match {
      case Some(host) => submitWorkflow(workflowDefine, host)
      case None => selectHost(workflowDefine.name).flatMap(submitWorkflow(workflowDefine, _))
    }
  }

  private def selectHost(key: String): Future[String] = {
    clusterNodes(status = Option(NodeStatus.UP), None, None).map(_.consistentHashSelect(key).host)
  }

  private def submitWorkflow(workflowDefine: WorkflowDefine, host: String): Future[Unit] = {
    workflowDefineRepo.save(workflowDefine.name, workflowDefine.copy(host = Option(host)))
  }

  override def runWorkflow(name: String, fallbackStrategy: FallbackStrategy, env: Map[String, Any], keepAlive: Boolean, scheduleTime: Option[Date], version: Option[Int]): Future[RunWorkflowInfo] = {
    getWorkflow(name).flatMap { wf =>

      if (version.getOrElse(Int.MaxValue) < wf.version) {
        stopWorkflowLocally(name)
        Future.failed(new VersionOutdatedException("workflow version is " + version.get + ", lower than " + wf.version))

      } else if (isLocal(wf.host)) {
        checkWorkflowStatus(name).flatMap(meta => {
          val newMeta = RichGraphMeta(meta).copy(
            id = meta.id + 1,
            rerunStrategy = RerunStrategy.NONE,
            env = meta.env ++ env,
            createTime = Option(new Date()),
            scheduleTime = scheduleTime,
            startTime = None,
            endTime = None,
            rerunId = 0,
            rerunFlag = false,
            host = Environment.HOST)
          runWorkflowLocally(name, newMeta, wf, keepAlive)
        })

      } else {
        RemoteRestServices.get(wf.host.get).runWorkflow(name, fallbackStrategy, env, keepAlive, scheduleTime, version)
      }
    }
  }

  protected def runWorkflowLocally(name: String, graphMeta: GraphMeta, workflowDefine: WorkflowDefine, keepAlive: Boolean): Future[RunWorkflowInfo]

  override def rerunWorkflow(name: String, reRunStrategy: RerunStrategy, env: Map[String, Any], keepAlive: Boolean): Future[RunWorkflowInfo] = {
    if (reRunStrategy == RerunStrategy.NONE) {
      Future.failed(new IllegalArgumentException("rerunStrategy can not be none"))
    }
    getWorkflow(name).flatMap { wf =>
      if (isLocal(wf.host)) {
        checkWorkflowStatus(name)
          .map(meta => {
            meta.graphStatus match {
              case GraphStatus.SUCCESS | GraphStatus.FAILED | GraphStatus.KILLED =>
                RichGraphMeta(meta).copy(
                  rerunStrategy = reRunStrategy,
                  env = meta.env ++ env,
                  startTime = None,
                  endTime = None,
                  rerunFlag = false,
                  host = Environment.HOST
                )
              case status =>
                throw new BadRequestException("workflow not complete，status is: " + status)
            }
          })
          .flatMap(runWorkflowLocally(name, _, wf, keepAlive))
          .andThen {
            case Success(value) => log.info("rerun workflow: " + value.name)
          }
      } else {
        RemoteRestServices.get(wf.host.get)
          .rerunWorkflow(name, reRunStrategy, env, keepAlive)
      }
    }
  }

  override def killWorkflow(name: String, id: Int): Future[Unit] = {
    getWorkflow(name).flatMap { wf =>
      if (isLocal(wf.host)) {
        killWorkflowLocally(name, id)
      } else {
        RemoteRestServices.get(wf.host.get).killWorkflow(name, id)
      }
    }
  }

  protected def killWorkflowLocally(name: String, id: Int): Future[Unit]

  override def stopWorkflow(name: String): Future[Unit] = {
    getWorkflow(name).flatMap { wf =>
      if (isLocal(wf.host)) {
        stopWorkflowLocally(name)
      } else {
        RemoteRestServices.get(wf.host.get).stopWorkflow(name)
      }
    }
  }

  protected def stopWorkflowLocally(name: String): Future[Unit]

  override def getWorkflow(name: String): Future[WorkflowDefine] = {
    isWorkflowAlive(name).flatMap { bool =>
      workflowDefineRepo.findById(name).map(_.get).map(_.copy(alive = bool))
    }
  }

  override def checkWorkflowStatus(name: String): Future[GraphMeta] = {
    val env = Map[String, AnyRef](
      "name" -> name,
      "sorts" -> Array(Sort("id", "DESC"), Sort("rerunId", "DESC")))
    val expr =
      """
        |repo.select(
        | repo.field('name') == name,
        | repo.sort(sorts),
        | repo.size(1)
        |)
        |""".stripMargin
    graphMetaRepo.find(expr, env).map { seq =>
      if (seq.isEmpty)
        new RichGraphMeta().copy(name = name, id = -1)
      else
        seq.head
    }
  }

  override def checkWorkflowStatus(name: String, id: Int): Future[GraphMeta] = {
    val env = Map[String, AnyRef]("name" -> name, "id" -> id.asInstanceOf[AnyRef])
    val expr =
      """
        |repo.select(
        | repo.field('name') == name && repo.field('id') == id,
        | repo.sort('reRunId', 'DESC'),
        | repo.size(1)
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
    coordinator.host match {
      case Some(host) => submitCoordinator(coordinator, host)
      case None => selectHost(coordinator.name).flatMap(submitCoordinator(coordinator, _))
    }
  }

  private def submitCoordinator(coordinator: Coordinator, host: String): Future[Unit] = {
    coordinatorRepo.save(coordinator.name, coordinator.copy(host = Option(host)))
      .flatMap { _ =>
        submitWorkflow(
          workflowDefine = coordinator.workflowDefine.copy(version = coordinator.version, name = coordinator.name, host = Option(host)),
          host = host
        )
      }
  }

  import scala.collection.JavaConversions.mapAsJavaMap
  override def recover(coordinator: Coordinator): Future[Unit] = {
    submitCoordinator(coordinator, Environment.HOST)
      .flatMap { _ =>
        checkCoordinatorStatus(coordinator.name)
      }
      .flatMap { s =>
        val env = Map("name" -> coordinator.name)
        coordinatorLogRepo.find(
          """
            |repo.select(
            | repo.field('name') == name,
            | repo.sort('timestamp', 'DESC'),
            | repo.size(1)
            |)
            |""".stripMargin, env)
          .map(_.head)
          .map(_.timestamp.getTime + 1)
          .map(new Date(_))
          .map(_ -> s)
      }
      .map { s =>
        coordinatorAction(coordinator.copy(startTime = Option(s._1)), recoverStatus = Option(s._2.status))
      }
  }

  override def runCoordinator(name: String): Future[Unit] = {
    getCoordinator(name).flatMap[Unit](runCoordinator)
  }

  private def runCoordinator(coordinator: Coordinator): Future[Unit] = {
    if (isLocal(coordinator.host)) {
      coordinatorAction(coordinator, recoverStatus = None)
      updateCoordinatorStatus(coordinator.name, CoordStatus.RUNNING)
    } else {
      RemoteRestServices.get(coordinator.host.get).runCoordinator(coordinator.name)
    }
  }

  def coordinatorAction(coordinator: Coordinator, recoverStatus: Option[CoordStatus]): Unit = {
    val name = coordinator.name
    val cancellable = coordinator.endTime match {
      case Some(date) =>
        val duration = FiniteDuration(date.getTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
        Scheduler.scheduleOnce(duration) {
          stopCoordinator(name)
        }(actorSystem)
      case None =>
        Cancellable.alreadyCancelled
    }
    val trigger = Triggers.getTrigger(coordinator.trigger.name)

    val action: (Date, Map[String, Any]) => Unit = { (date, env) =>
      log.info("trigger running Workflow: " + name)
      runWorkflow(name, coordinator.fallbackStrategy, coordinator.workflowDefine.env ++ env, keepAlive = true, scheduleTime = Some(date), Some(coordinator.version))
        .andThen {
          case Failure(_: VersionOutdatedException) =>
            log.warning("coordinator outdated: " + coordinator)
            trigger.stop(name)
            cancellable.cancel()
          case Failure(e) =>
            log.error(e, "trigger workflow failed: " + name)
        }
        .onComplete {
          updateCoordinatorLog(coordinator, _)
        }
    }
    recoverStatus match {
      case Some(status) =>
        trigger.recover(name, coordinator.trigger.config, coordinator.startTime, status)(action)
      case None =>
        trigger.start(name, coordinator.trigger.config, coordinator.startTime)(action)
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
    getCoordinator(name).flatMap { coord =>
      if (isLocal(coord.host)) {
        Triggers.getTrigger(coord.trigger.name).suspend(name).flatMap {
          case true => updateCoordinatorStatus(name, CoordStatus.SUSPENDED).map(_ => true)
          case false => Future(false)
        }
      } else {
        RemoteRestServices.get(coord.host.get).suspendCoordinator(name)
      }
    }
  }

  override def stopCoordinator(name: String): Future[Boolean] = {
    getCoordinator(name).flatMap { coord =>
      if (isLocal(coord.host)) {
        Triggers.getTrigger(coord.trigger.name).stop(name).flatMap {
          case true => updateCoordinatorStatus(name, CoordStatus.STOPPED).map(_ => true)
          case false => Future(false)
        }
      } else {
        RemoteRestServices.get(coord.host.get).stopCoordinator(name)
      }
    }
  }

  override def resumeCoordinator(name: String): Future[Boolean] = {
    getCoordinator(name)
      .flatMap { coord =>
        if (isLocal(coord.host)) {
          Triggers.getTrigger(coord.trigger.name).resume(name).flatMap {
            case true => updateCoordinatorStatus(name, CoordStatus.RUNNING).map(_ => true)
            case false => Future(false)
          }
        } else {
          RemoteRestServices.get(coord.host.get).resumeCoordinator(name)
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

  override def searchWorkflows(req: SearchRequest): Future[Page[GraphMeta]] = {
    val (newExpr, newEnv) = createExpr(req)
    graphMetaRepo.find(newExpr, newEnv).map(Page(_, req.size, req.page))
  }

  override def searchCoordinators(req: SearchRequest): Future[Page[CoordinatorStatus]] = {
    val (newExpr, newEnv) = createExpr(req)
    coordinatorStateRepo.find(newExpr, newEnv).map(Page(_, req.size, req.page))
  }

  override def checkCoordinatorStatus(name: String): Future[CoordinatorStatus] = {
    coordinatorStateRepo.findById(name).map(_.get)
  }

  private def updateCoordinatorStatus(name: String, state: CoordStatus): Future[Unit] = {
    coordinatorStateRepo.save(name, CoordinatorStatus(name, state, latestUpdateTime = new Date()))
  }

  private def createExpr(req: SearchRequest): (String, JavaMap[String, AnyRef]) = {
    val env = new JavaHashMap[String, AnyRef](req.getEnv)
    env.put("expr", Evaluator.rawExecute(req.expr, env).asInstanceOf[AnyRef])
    env.put("sorts", req.sorts.toArray)
    env.put("size", req.size.asInstanceOf[AnyRef])
    env.put("page", req.page.asInstanceOf[AnyRef])
    val expr0 =
      """
        |repo.select(
        | expr,
        | repo.sort(sorts),
        | repo.size(size),
        | repo.page(page)
        |)
        |""".stripMargin
    (expr0, env)
  }
}