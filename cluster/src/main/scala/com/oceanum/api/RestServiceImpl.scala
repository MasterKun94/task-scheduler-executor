package com.oceanum.api

import akka.actor.ActorSystem
import akka.cluster.{Cluster, MemberStatus}
import akka.stream.QueueOfferResult.{Dropped, Enqueued, QueueClosed, Failure => QFailure}
import com.oceanum.annotation.IRestService
import com.oceanum.api.HttpServer.restService
import com.oceanum.api.entities.{ClusterNode, ClusterNodes, Page, NodeTaskInfo, RunWorkflowInfo, WorkflowDefine}
import com.oceanum.client.TaskClient
import com.oceanum.common.{ActorSystems, Environment, GraphMeta, NodeStatus}
import com.oceanum.exec.RunnerManager
import com.oceanum.graph.{GraphHook, GraphMetaHandler, Workflow, WorkflowInstance}
import com.oceanum.trigger.Triggers

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future

@IRestService(priority = 1)
class RestServiceImpl extends AbstractRestService {
  import Environment.NONE_BLOCKING_EXECUTION_CONTEXT
  private implicit val client: TaskClient = TaskClient.create(actorSystem, Environment.CLUSTER_NODE_SEEDS)
  private val workflows: TrieMap[String, WorkflowInstance] = TrieMap()
  private val hooks: TrieMap[(String, Int), GraphHook] = TrieMap()
  private val cluster = Cluster(ActorSystems.SYSTEM)

  override protected def runWorkflowLocally(name: String, graphMeta: GraphMeta, workflowDefine: WorkflowDefine, keepAlive: Boolean): Future[RunWorkflowInfo] = {
    val instance = workflows.getOrElseUpdate(name, {
      implicit val graphMetaHandler: GraphMetaHandler = new GraphMetaHandler {
        override def onComplete(graphMeta: GraphMeta): Unit = if (!keepAlive) stopWorkflow(name)
        override def close(): Unit = workflows.remove(name)
      }
      Workflow.fromGraph(workflowDefine).run()
    })

    instance.offer(graphMeta).map {
      case (Enqueued, hook) =>
        hooks += ((name, graphMeta.id) -> hook)
        RunWorkflowInfo.from(graphMeta)

      case (Dropped, _) =>
        throw new RuntimeException("message been dropped")

      case (QFailure(e), _) =>
        throw e

      case (QueueClosed, _) =>
        throw new RuntimeException("queue closed")
    }
  }

  override def killWorkflowLocally(name: String, id: Int): Future[Unit] = {
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

  override def stopWorkflowLocally(name: String): Future[Unit] = {
    killWorkflow(name, -1)
      .map { _ =>
        workflows.remove(name).foreach(_.complete())
      }
  }

  override def actorSystem: ActorSystem = ActorSystems.SYSTEM

  override def isWorkflowAlive(name: String): Future[Boolean] = Future.successful(workflows.contains(name))

  override def clusterNodes(status: Option[NodeStatus], host: Option[String], role: Option[String]): Future[ClusterNodes] = {
    Future.successful {
      val members = cluster.state.members
      val members1 = status match {
        case Some(s) =>
          val memberStatus = statusMap(s)
          members.filter(_.status == memberStatus)
        case None => members
      }
      val members2 = host match {
        case Some(host) => members1.filter(_.uniqueAddress.address.host.get.equals(host))
        case None => members
      }
      val members3 = role match {
        case Some(role) => members2.filter(_.roles.contains(role))
        case None => members
      }
      ClusterNodes(nodes = members3.map(member => ClusterNode(
        host = member.uniqueAddress.address.host.get,
        status = reverseStatusMap(member.status),
        roles = member.roles.toSeq
      )).toSeq
      )
    }
  }

  private val statusMap = Map[NodeStatus, MemberStatus](
    NodeStatus.UP -> MemberStatus.Up,
    NodeStatus.DOWN -> MemberStatus.Down,
    NodeStatus.REMOVED -> MemberStatus.Removed,
    NodeStatus.JOINING -> MemberStatus.Joining,
    NodeStatus.LEAVING -> MemberStatus.Leaving,
    NodeStatus.EXITING -> MemberStatus.Exiting,
  NodeStatus.WEAKLY_UP -> MemberStatus.WeaklyUp
  )

  private val reverseStatusMap = Map[MemberStatus, NodeStatus](
    MemberStatus.Up -> NodeStatus.UP,
    MemberStatus.Down -> NodeStatus.DOWN,
    MemberStatus.Removed -> NodeStatus.REMOVED,
    MemberStatus.Joining -> NodeStatus.JOINING,
    MemberStatus.Leaving ->  NodeStatus.LEAVING,
    MemberStatus.Exiting -> NodeStatus.EXITING,
    MemberStatus.WeaklyUp -> NodeStatus.WEAKLY_UP
  )

  override def clusterTaskInfos(host: Option[String]): Future[Page[NodeTaskInfo]] = {
    val nodes = clusterNodes(status = Some(NodeStatus.UP), host = host)
    nodes
      .flatMap { nodes =>
        Future.sequence(nodes.nodes.map(node => nodeTaskInfo(node.host)))
      }
      .map(seq => Page(seq, size = seq.size, 0))
  }

  override def nodeTaskInfo(host: String): Future[NodeTaskInfo] = {
    if (host.equals(Environment.HOST)) {
      Future(RunnerManager.getTaskInfo)
    } else {
      RemoteRestServices.get(host).nodeTaskInfo(host)
    }
  }
}
