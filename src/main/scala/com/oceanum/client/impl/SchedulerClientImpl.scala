package com.oceanum.client.impl

import akka.actor.{Actor, ActorRef, ActorSystem, PoisonPill, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.oceanum.client.actors.{ClientEndpoint, ClientInstance, HandlerActor}
import com.oceanum.client._
import com.oceanum.cluster.exec.State
import com.oceanum.common._

import scala.concurrent.{ExecutionContext, Future, Promise}

class SchedulerClientImpl(endpoint: ActorRef, system: ActorSystem)(implicit executionContext: ExecutionContext, timeout: Timeout) extends SchedulerClient {
  private lazy val metricsClient = system.actorOf(Props(classOf[ClientEndpoint], endpoint), "metrics-client")

  override def execute(task: Task, stateHandler: StateHandler): Future[TaskInstance] = {
    doExecute(AvailableExecutorRequest(task.topic), task, stateHandler).map(_.head)
  }

  override def executeAll(task: Task, stateHandler: StateHandler, timeWait: String): Future[Seq[TaskInstance]] = {
    doExecute(AvailableExecutorsRequest(task.topic, timeWait), task, stateHandler)
  }

  private def getClient(implicit executionContext: ExecutionContext): ActorRef = {
    system.actorOf(Props(classOf[ClientEndpoint], endpoint))
  }

  private def doExecute(requestMsg: Message, task: Task, stateHandler: StateHandler): Future[Seq[TaskInstance]] = {
    val client = getClient
    val promise = Promise[State]()
    client
      .ask(requestMsg)
      .mapTo[AvailableExecutorResponse]
      .map(response => {
        val res: Seq[ActorRef] = response
          .executor
          .map(executor => system.actorOf(Props(classOf[ClientInstance], executor.actor, task, stateHandler, promise)))
        res.map(TaskInstance(_, promise.future))
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