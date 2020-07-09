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

  def broadcastExecute(task: Task,
                       stateHandler: StateHandler = StateHandler.default(),
                       timeWait: String = "3s"): Future[Seq[TaskInstance]]

  def handleClusterMetrics(interval: String)(handler: ClusterMetricsResponse => Unit): ShutdownHook

  def handleClusterInfo(interval: String)(handler: ClusterStateResponse => Unit): ShutdownHook

  def handleTaskInfo(handler: NodeTaskInfoResponse => Unit): ShutdownHook

  def close: Future[Unit]
}

object SchedulerClient {
  private val clients: TrieMap[ActorSystem, SchedulerClient] = TrieMap()

  def apply(host: String, port: Int, seedNodes: String, configFile: String)(implicit timeout: Timeout = Timeout(20, TimeUnit.SECONDS)): SchedulerClient = {
    import Environment.Arg
    Environment.loadArgs(Array(s"${Arg.CONF}=$configFile", s"${Arg.SEED_NODE}=$seedNodes", s"${Arg.HOST}=$host", s"${Arg.CLIENT_PORT}=$port"))
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
