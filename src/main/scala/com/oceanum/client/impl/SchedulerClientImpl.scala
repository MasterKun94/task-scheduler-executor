package com.oceanum.client.impl

import akka.actor.{Actor, ActorPaths, ActorRef, ActorSystem, PoisonPill, Props}
import akka.cluster.client.{ClusterClient, ClusterClientSettings}
import akka.pattern.ask
import akka.util.Timeout
import com.oceanum.client.actors.{ClientInstance, ClientListener, ClientEndpoint, HandlerActor}
import com.oceanum.client.{SchedulerClient, ShutdownHook, StateHandler, Task, TaskInstance}
import com.oceanum.common._

import scala.concurrent.{ExecutionContext, Future}

class SchedulerClientImpl(endpoint: ActorRef, system: ActorSystem)(implicit executionContext: ExecutionContext, timeout: Timeout) extends SchedulerClient {
  private lazy val metricsClient = system.actorOf(Props(classOf[ClientEndpoint], endpoint), "metrics-client")

  override def execute(task: Task, stateHandler: StateHandler): Future[TaskInstance] = {
    doExecute(AvailableExecutorRequest(task.topic), task, stateHandler)
  }

  override def executeAll(task: Task, stateHandler: StateHandler, timeWait: String): Future[TaskInstance] = {
    doExecute(AvailableExecutorsRequest(task.topic, timeWait), task, stateHandler)
  }

  private def getClient(implicit executionContext: ExecutionContext): ActorRef = {
    system.actorOf(Props(classOf[ClientEndpoint], endpoint), "client-actor")
  }

  private def doExecute(requestMsg: Message, task: Task, stateHandler: StateHandler): Future[TaskInstance] = {
    val client = getClient
    client
      .ask(requestMsg)
      .mapTo[AvailableExecutorResponse]
      .flatMap(response => {
        val res = response.executor
          .map(executor => system.actorOf(Props(classOf[ClientInstance], executor.actor)))
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
    val handlerActor = system.actorOf(Props(new HandlerActor(_ => receive)))
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
    val handlerActor = system.actorOf(Props(new HandlerActor(_ => receive)))
    metricsClient.tell(ClusterStateRequest(interval, interval), handlerActor)
    new ShutdownHook {
      override def kill(): Future[Boolean] = {
        handlerActor ! PoisonPill
        metricsClient.ask(ClusterStateStopRequest(handlerActor)).mapTo
      }
    }
  }

  override def handleTaskInfo(handler: NodeTaskInfoResponse => Unit): ShutdownHook = {
    val receive: Actor.Receive = {
      case res: NodeTaskInfoResponse => handler(res)
    }
    val handlerActor = system.actorOf(Props(new HandlerActor(_ => receive)))
    metricsClient.tell(NodeTaskInfoRequest("", ""), handlerActor)
    new ShutdownHook {
      override def kill(): Future[Boolean] = {
        handlerActor ! PoisonPill
        metricsClient.ask(NodeTaskInfoStopRequest(handlerActor)).mapTo
      }
    }
  }

  override def close: Future[Unit] = system.terminate().map(_ => Unit)
}