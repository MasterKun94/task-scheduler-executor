package com.oceanum.api

import akka.actor.ActorSystem
import akka.cluster.{Cluster, MemberStatus}
import akka.stream.QueueOfferResult.{Dropped, Enqueued, QueueClosed, Failure => QFailure}
import com.oceanum.annotation.IRestService
import com.oceanum.api.HttpServer.restService
import com.oceanum.api.entities.{ClusterNode, ClusterNodes, NodeTaskInfo, NodeTaskInfos, RunWorkflowInfo, WorkflowDefine}
import com.oceanum.client.TaskClient
import com.oceanum.common.{ActorSystems, Environment, GraphMeta}
import com.oceanum.exec.RunnerManager
import com.oceanum.graph.{GraphHook, GraphMetaHandler, Workflow, WorkflowInstance}

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

  override def getClusterNodes(status: Option[String], host: Option[String], role: Option[String]): Future[ClusterNodes] = {
    Future.successful {
      val members = cluster.state.members
      val members1 = status match {
        case Some(status) =>
          val memberStatus = statusMap(status.toLowerCase())
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

  private val statusMap = Map[String, MemberStatus](
    "up" -> MemberStatus.Up,
    "down" -> MemberStatus.Down,
    "removed" -> MemberStatus.Removed,
    "joining" -> MemberStatus.Joining,
    "leaving" -> MemberStatus.Leaving,
    "exiting" -> MemberStatus.Exiting,
    "weaklyup" -> MemberStatus.WeaklyUp
  )

  private val reverseStatusMap = Map[MemberStatus, String](
    MemberStatus.Up -> "up",
    MemberStatus.Down -> "down",
    MemberStatus.Removed -> "removed",
    MemberStatus.Joining -> "joining",
    MemberStatus.Leaving ->  "leaving",
    MemberStatus.Exiting -> "exiting",
    MemberStatus.WeaklyUp -> "weaklyup"
  )

  override def getClusterTaskInfos(host: Option[String]): Future[NodeTaskInfos] = {
    val nodes = getClusterNodes(status = Some("up"), host, None)
    nodes
      .flatMap { nodes =>
        Future.sequence(nodes.nodes.map(node => getNodeTaskInfo(node.host)))
      }
      .map(NodeTaskInfos)
  }

  override def getNodeTaskInfo(host: String): Future[NodeTaskInfo] = {
    if (host.equals(Environment.HOST)) {
      Future(RunnerManager.getTaskInfo)
    } else {
      RemoteRestServices.get(host).getNodeTaskInfo(host)
    }
  }
}
