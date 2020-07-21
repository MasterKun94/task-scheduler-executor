package com.oceanum.client

import java.util.concurrent.TimeUnit

import akka.pattern.ask
import akka.actor.{Actor, ActorPaths, ActorRef, ActorSystem, PoisonPill, Props}
import akka.cluster.client.ClusterClient.Publish
import akka.cluster.client.{ClusterClient, ClusterClientSettings}
import akka.util.Timeout
import com.oceanum.client.actors.{ClientEndpoint, ClientInstance, ClientListener, HandlerActor}
import com.oceanum.exec.State
import com.oceanum.common.{AvailableExecutorRequest, AvailableExecutorResponse, AvailableExecutorsRequest, ClusterInfoMessageHolder, ClusterMessage, ClusterMetrics, ClusterMetricsRequest, ClusterState, ClusterStateRequest, Environment, Message, NodeTaskInfo, NodeTaskInfoRequest, StopRequest}

import scala.collection.concurrent.TrieMap
import scala.concurrent.{ExecutionContext, Future, Promise}

/**
 * @author chenmingkun
 * @date 2020/5/4
 */
class TaskClient(endpoint: ActorRef, val system: ActorSystem)(implicit executionContext: ExecutionContext, timeout: Timeout) {

  def execute(task: Task, stateHandler: StateHandler = StateHandler.default()): SingleTaskInstanceRef = {
    new SingleTaskInstanceRef(doExecute(AvailableExecutorRequest(task.topic), task, stateHandler).map(_.head))
  }

  def broadcastExecute(task: Task, stateHandler: StateHandler = StateHandler.default(), timeWait: String = "10s"): MultiTaskInstanceRef = {
    new MultiTaskInstanceRef(doExecute(AvailableExecutorsRequest(task.topic, timeWait), task, stateHandler))
  }

  def getClient(implicit executionContext: ExecutionContext): ActorRef = {
    system.actorOf(Props(classOf[ClientEndpoint], endpoint))
  }

  def doExecute(requestMsg: Message, task: Task, handler: StateHandler): Future[Seq[TaskInstance]] = {
    val client = getClient
    val promise = Promise[State]()
    client
      .ask(requestMsg)
      .mapTo[AvailableExecutorResponse]
      .map(response => {
        val res: Seq[ActorRef] = response
          .executor
          .map(executor => system.actorOf(Props(classOf[ClientInstance], executor.actor, task, handler, promise)))
        res.map(TaskInstance(_, promise.future))
      })
  }

  def handleClusterMetrics(interval: String)(handler: ClusterMetrics => Unit): ShutdownHook = {
    subscribe(ClusterMetricsRequest(interval, interval)) {
      case res: ClusterMetrics => handler(res)
    }
  }

  def handleClusterInfo(interval: String)(handler: ClusterState => Unit): ShutdownHook = {
    subscribe(ClusterStateRequest(interval, interval)) {
      case res: ClusterState => handler(res)
    }
  }

  def handleTaskInfo(handler: NodeTaskInfo => Unit): ShutdownHook = {
    subscribe(NodeTaskInfoRequest("", "")) {
      case res: NodeTaskInfo => handler(res)
    }
  }

  def close: Future[Unit] = system.terminate().map(_ => Unit)

  private def subscribe(message: ClusterMessage)(receive: Actor.Receive): ShutdownHook = {
    val handlerActor = system.actorOf(Props(new HandlerActor(_ => receive)))
    val publish = Publish(Environment.CLUSTER_NODE_METRICS_TOPIC, ClusterInfoMessageHolder(message, handlerActor))
    endpoint ! publish
    new ShutdownHook {
      override def kill(): Future[Boolean] = {
        handlerActor ! PoisonPill
        endpoint.ask(StopRequest(handlerActor)).mapTo
      }
    }
  }
}

object TaskClient {
  private val clients: TrieMap[ActorSystem, TaskClient] = TrieMap()

  def apply(host: String, port: Int, seedNodes: String, configFile: String)(implicit timeout: Timeout = Timeout(20, TimeUnit.SECONDS)): TaskClient = {
    import Environment.Arg
    Environment.loadArgs(Array(s"${Arg.CONF}=$configFile", s"${Arg.SEED_NODE}=$seedNodes", s"${Arg.HOST}=$host", s"${Arg.CLIENT_PORT}=$port"))
    val system = Environment.CLIENT_SYSTEM
    TaskClient.create(system, Environment.CLUSTER_NODE_SEEDS)
  }

  def create(system: ActorSystem, seedNodes: Seq[String])(implicit timeout: Timeout = Timeout(20, TimeUnit.SECONDS)): TaskClient = {
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
      val client = new TaskClient(endpoint, system)(executionContext, timeout)
      clients.put(system, client)
      client
    })
  }
}
