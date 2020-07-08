package com.oceanum.client

import java.util.concurrent.TimeUnit

import akka.actor.{ActorPaths, ActorSystem, Props}
import akka.cluster.client.{ClusterClient, ClusterClientSettings}
import akka.util.Timeout
import com.oceanum.client.actors.ClientListener
import com.oceanum.client.impl.SchedulerClientImpl
import com.oceanum.common.{ClusterMetricsResponse, ClusterStateResponse, Environment, NodeTaskInfoResponse}

import scala.collection.concurrent.TrieMap
import scala.concurrent.{ExecutionContext, Future}

/**
 * @author chenmingkun
 * @date 2020/5/4
 */
trait SchedulerClient {
  def execute(task: Task,
              stateHandler: StateHandler = StateHandler.default()): Future[TaskInstance]

  def executeAll(task: Task,
                 stateHandler: StateHandler = StateHandler.default(),
                 timeWait: String = "3s"): Future[TaskInstance]

  def handleClusterMetrics(interval: String)(handler: ClusterMetricsResponse => Unit): ShutdownHook

  def handleClusterInfo(interval: String)(handler: ClusterStateResponse => Unit): ShutdownHook

  def handleTaskInfo(handler: NodeTaskInfoResponse => Unit): ShutdownHook

  def close: Future[Unit]
}

object SchedulerClient {
  private val clients: TrieMap[ActorSystem, SchedulerClient] = TrieMap()

  def apply(host: String, port: Int, seedNodes: String, configFile: String)(implicit timeout: Timeout = Timeout(20, TimeUnit.SECONDS)): SchedulerClient = {
    Environment.loadArgs(Array(s"--conf=$configFile"))
    Environment.load(Environment.Key.HOST, host)
    Environment.load(Environment.Key.CLIENT_NODE_PORT, port.toString)
    val seeds = seedNodes
      .split(",")
      .map(_.trim)
      .filter(_.nonEmpty)
      .map(_.split(":"))
      .map(arr => if (arr.length == 1) arr :+ "3551" else arr)
      .map(_.mkString(":"))
    Environment.load(Environment.Key.CLUSTER_NODE_SEEDS, seeds.mkString(","))
    val system = Environment.CLIENT_SYSTEM
    SchedulerClient.create(system, Environment.CLUSTER_NODE_SEEDS)
  }

  def create(system: ActorSystem, seedNodes: Seq[String])(implicit timeout: Timeout = Timeout(20, TimeUnit.SECONDS)): SchedulerClient = {
    clients.getOrElse(system, {
      val executionContext = ExecutionContext.global
      val endpoint = {
        //先放一个contact-point, 系统会自动增加其它的点
        val initialContacts = seedNodes
          .map(s => s + "/system/receptionist")
          .map(ActorPaths.fromString)
          .toSet
        val client = system.actorOf(
          ClusterClient.props(
            ClusterClientSettings(system)
              .withInitialContacts(initialContacts)),
          "client-endpoint")
        system.actorOf(Props(classOf[ClientListener], client), "client-event-listener")
        client
      }
      val client = new SchedulerClientImpl(endpoint, system)(executionContext, timeout)
      clients.put(system, client)
      client
    })
  }
}
