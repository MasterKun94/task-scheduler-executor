package com.oceanum.api

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.stream.QueueOfferResult.{Dropped, Enqueued, QueueClosed, Failure => QFailure}
import com.oceanum.annotation.IRestService
import com.oceanum.api.entities.{Coordinator, RunWorkflowInfo}
import com.oceanum.client.TaskClient
import com.oceanum.common.{ActorSystems, Environment, GraphMeta, Scheduler}
import com.oceanum.graph.{GraphHook, GraphMetaHandler, Workflow, WorkflowInstance}
import com.oceanum.triger.Triggers

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success, Try}

@IRestService(priority = 1)
class RestServiceImpl extends AbstractRestService {
  import Environment.NONE_BLOCKING_EXECUTION_CONTEXT
  private implicit val client: TaskClient = TaskClient.create(actorSystem(), Environment.CLUSTER_NODE_SEEDS)
  private val workflows: TrieMap[String, WorkflowInstance] = TrieMap()
  private val hooks: TrieMap[(String, Int), GraphHook] = TrieMap()

  override protected def runWorkflow(name: String, graphMeta: GraphMeta, keepAlive: Boolean): Future[RunWorkflowInfo] = {
    val future = workflows.get(name) match {
      case Some(workflow) =>
        Future.successful(workflow)
      case None =>
        implicit val graphMetaHandler: GraphMetaHandler = new GraphMetaHandler {
          override def onComplete(graphMeta: GraphMeta): Unit = if (!keepAlive) stopWorkflow(name)
          override def close(): Unit = workflows.remove(name)
        }
        getWorkflow(name)
          .map(Workflow.fromGraph)
          .map(_.run())
          .andThen {
            case Success(value) =>
              workflows += (name -> value)
            case _ =>
          }
    }

    future.flatMap { instance =>
      instance.offer(graphMeta).map {
        case (Enqueued, hook) =>
          hooks += ((name, graphMeta.id) -> hook)
          RunWorkflowInfo.from(graphMeta)

        case (Dropped, _) =>
          throw new Exception("message been dropped")

        case (QFailure(e), _) =>
          throw e

        case (QueueClosed, _) =>
          throw new Exception("queue closed")
      }
    }
  }

  override def killWorkflow(name: String, id: Int): Future[Unit] = {
    if (id == -1) {
      Future
        .sequence(hooks
          .takeWhile(_._1._1.equals(name))
          .keys
          .map(key => killWorkflow(key._1, key._2))
        )
        .map(_ => Unit)
    } else {
      Future.sequence(hooks.remove((name, id)).map(_.kill()).toSeq).map(_ => Unit)
    }
  }

  override def stopWorkflow(name: String): Future[Unit] = {
    killWorkflow(name, -1)
      .andThen {
        case _ =>
          workflows.remove(name).foreach(_.complete())
      }
  }

  override def actorSystem: ActorSystem = ActorSystems.SYSTEM

  override def isWorkflowAlive(name: String): Future[Boolean] = Future.successful(workflows.contains(name))
}
