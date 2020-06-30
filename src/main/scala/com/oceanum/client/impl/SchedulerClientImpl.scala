package com.oceanum.client.impl

import akka.actor.{Actor, ActorPaths, ActorRef, PoisonPill, Props}
import akka.cluster.client.{ClusterClient, ClusterClientSettings}
import akka.pattern.ask
import akka.util.Timeout
import com.oceanum.ShutdownHook
import com.oceanum.client.actors.{ClientExecutor, ClientListener, ExecutorFinder, HandlerActor}
import com.oceanum.client.{SchedulerClient, StateHandler, Task, TaskInstance}
import com.oceanum.common._

import scala.concurrent.{ExecutionContext, Future}

class SchedulerClientImpl(endpoint: ActorRef)(implicit executionContext: ExecutionContext, timeout: Timeout) extends SchedulerClient {
  private lazy val metricsClient = Environment.CLIENT_SYSTEM.actorOf(Props(classOf[ExecutorFinder], endpoint), "metrics-client")

  override def execute(topic: String, task: Task, stateHandler: StateHandler = StateHandler.empty()): Future[TaskInstance] = {
    doExecute(AvailableExecutorRequest(topic), task, stateHandler)
  }

  override def executeAll(topic: String, task: Task, stateHandler: StateHandler = StateHandler.empty(), timeWait: String): Future[TaskInstance] = {
    doExecute(AvailableExecutorsRequest(topic, timeWait), task, stateHandler)
  }

  private def getClient(implicit executionContext: ExecutionContext): ActorRef = {
    Environment.CLIENT_SYSTEM.actorOf(Props(classOf[ExecutorFinder], endpoint), "client-actor")
  }

  private def doExecute(requestMsg: Any, task: Task, stateHandler: StateHandler = StateHandler.empty()): Future[TaskInstance] = {
    val client = getClient
    client
      .ask(requestMsg)
      .mapTo[AvailableExecutorResponse]
      .flatMap(response => {
        val res = response.executor
          .map(executor => Environment.CLIENT_SYSTEM.actorOf(Props(classOf[ClientExecutor], executor.actor)))
          .map(client => client
            .ask(ExecuteOperatorRequest(task, stateHandler))
            .map(_ => client)
          )
          .toSeq
        Future.sequence(res).map(TaskInstance(_))
      })
  }

  override def handleClusterMetrics(interval: String)(handler: ClusterMetricsResponse => Unit): ShutdownHook = {
    val receive: Actor.Receive = {
      case res: ClusterMetricsResponse => handler(res)
    }
    val handlerActor = Environment.CLIENT_SYSTEM.actorOf(Props(new HandlerActor(_ => receive)))
    metricsClient.tell(ClusterMetricsRequest(interval, interval), handlerActor)
    new ShutdownHook {
      override def kill(): Future[Boolean] = {
        handlerActor ! PoisonPill
        metricsClient.ask(ClusterMetricsStopRequest(handlerActor)).mapTo
      }
    }
  }

  override def handleClusterInfo(interval: String)(handler: ClusterStateResponse => Unit): ShutdownHook = {
    val receive: Actor.Receive = {
      case res: ClusterStateResponse => handler(res)
    }
    val handlerActor = Environment.CLIENT_SYSTEM.actorOf(Props(new HandlerActor(_ => receive)))
    metricsClient.tell(ClusterStateRequest(interval, interval), handlerActor)
    new ShutdownHook {
      override def kill(): Future[Boolean] = {
        handlerActor ! PoisonPill
        metricsClient.ask(ClusterStateStopRequest(handlerActor)).mapTo
      }
    }
  }

  override def handleTaskInfo(interval: String)(handler: NodeTaskInfoResponse => Unit): ShutdownHook = {
    val receive: Actor.Receive = {
      case res: NodeTaskInfoResponse => handler(res)
    }
    val handlerActor = Environment.CLIENT_SYSTEM.actorOf(Props(new HandlerActor(_ => receive)))
    metricsClient.tell(NodeTaskInfoRequest(interval, interval), handlerActor)
    new ShutdownHook {
      override def kill(): Future[Boolean] = {
        handlerActor ! PoisonPill
        metricsClient.ask(NodeTaskInfoStopRequest(handlerActor)).mapTo
      }
    }
  }
}

object SchedulerClientImpl {
  private lazy val clientSystem = Environment.CLIENT_SYSTEM
  private lazy val clusterClient = {
    //先放一个contact-point, 系统会自动增加其它的点
    val initialContacts = Environment.CLUSTER_NODE_SEEDS
      .map(s => s + "/system/receptionist")
      .map(ActorPaths.fromString)
      .toSet
    val client = clientSystem.actorOf(
      ClusterClient.props(
        ClusterClientSettings(clientSystem)
          .withInitialContacts(initialContacts)),
      "client-endpoint")
    clientSystem.actorOf(Props(classOf[ClientListener], client), "client-event-listener")
    client
  }


  def apply(host: String, port: Int, seedNodes: String)(implicit timeout: Timeout): SchedulerClient = {

    import ExecutionContext.Implicits.global
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
    new SchedulerClientImpl(clusterClient)
  }
}
