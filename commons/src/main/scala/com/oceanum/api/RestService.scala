package com.oceanum.api

import java.util.Date

import com.oceanum.api.entities._
import com.oceanum.common._
import com.oceanum.jdbc.expr.{JavaHashMap, JavaMap}

import scala.collection.mutable
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

  def clusterTaskInfos(host: Option[String]): Future[Page[NodeTaskInfo]]

  def searchWorkflows(searchRequest: SearchRequest): Future[Page[GraphMeta]]

  def searchCoordinators(searchRequest: SearchRequest): Future[Page[CoordinatorStatus]]

  def nodeTaskInfo(host: String): Future[NodeTaskInfo]

  def listWorkflows(host: Option[String], graphStatus: Option[GraphStatus], sorts: Seq[Sort], size: Int, page: Int): Future[Page[GraphMeta]] = {
    val env: mutable.Map[String, Any] = mutable.Map()
    val expr = (host, graphStatus) match {
      case (Some(h), Some(s)) =>
        env.put("host", h)
        env.put("graphStatus", s)
        "repo.field('host') == host && repo.field('graphStatus') == graphStatus"
      case (Some(h), None) =>
        env.put("host", h)
        "repo.field('host') == host"
      case (None, Some(s)) =>
        env.put("graphStatus", s)
        "repo.field('graphStatus') == graphStatus"
      case (None, None) =>
        "repo.findAll()"
    }

    searchWorkflows(SearchRequest(expr, sorts, size, page, env.toMap))
  }

  def listCoordinators(host: Option[String], status: Option[CoordStatus], sorts: Seq[Sort], size: Int, page: Int): Future[Page[CoordinatorStatus]] = {
    val env: mutable.Map[String, Any] = mutable.Map()
    val expr = (host, status) match {
      case (Some(h), Some(s)) =>
        env.put("host", h)
        env.put("status", s)
        "repo.field('host') == host && repo.field('status') == status"
      case (Some(h), None) =>
        env.put("host", h)
        "repo.field('host') == host"
      case (None, Some(s)) =>
        env.put("status", s)
        "repo.field('status') == status"
      case (None, None) =>
        "repo.findAll()"
    }

    searchCoordinators(SearchRequest(expr, sorts, size, page, env.toMap))
  }

}

object RestService {
}
