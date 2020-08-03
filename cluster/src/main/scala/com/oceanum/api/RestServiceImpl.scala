package com.oceanum.api
import akka.stream.QueueOfferResult.{Dropped, Enqueued, Failure, QueueClosed}
import com.oceanum.api.entities.RunWorkflowInfo
import com.oceanum.client.TaskClient
import com.oceanum.common.{ActorSystems, Environment, GraphMeta}
import com.oceanum.graph.{GraphHook, GraphMetaHandler, Workflow, WorkflowInstance}

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future
import scala.util.Success

class RestServiceImpl extends AbstractRestService {
  import Environment.NONE_BLOCKING_EXECUTION_CONTEXT
  private implicit val client: TaskClient = TaskClient.create(ActorSystems.SYSTEM, Environment.CLUSTER_NODE_SEEDS)
  private val workflows: TrieMap[String, WorkflowInstance] = TrieMap()
  private val hooks: TrieMap[String, GraphHook] = TrieMap()

  override protected def runWorkflow(name: String, graphMeta: GraphMeta, keepAlive: Boolean): Future[RunWorkflowInfo] = {
    val future = workflows.get(name) match {
      case Some(workflow) =>
        Future.successful(workflow)
      case None =>
        implicit val graphMetaHandler: GraphMetaHandler = new GraphMetaHandler {
          override def onComplete(graphMeta: GraphMeta): Unit = {
            if (!keepAlive) {
              stopWorkflow(name)
            }
          }
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
          hooks += (name -> hook)
          RunWorkflowInfo.from(graphMeta)

        case (Dropped, _) =>
          throw new Exception("message been dropped")

        case (Failure(e), _) =>
          throw e

        case (QueueClosed, _) =>
          throw new Exception("queue closed")
      }
    }
  }

  override def killWorkflow(name: String): Future[Unit] = {
    Future.sequence(hooks.remove(name).map(_.kill())).map(_ => Unit)
  }

  override def stopWorkflow(name: String): Future[Unit] = {
    killWorkflow(name).andThen {
      case _ =>
        workflows.remove(name).foreach(_.complete())
        hooks.remove(name)
    }
  }

  override def runCoordinator(name: String): Unit = ???

  override def killCoordinator(name: String): Future[Unit] = ???

  override def suspendCoordinator(name: String): Future[Unit] = ???

  override def resumeCoordinator(name: String, discardFormerWorkflows: Boolean): Future[Unit] = ???
}
