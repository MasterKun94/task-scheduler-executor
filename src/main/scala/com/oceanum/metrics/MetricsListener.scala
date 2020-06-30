package com.oceanum.metrics

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable, PoisonPill, Props}
import akka.cluster.ClusterEvent.{MemberEvent, UnreachableMember}
import akka.cluster.metrics.{ClusterMetricsChanged, ClusterMetricsExtension}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, Unsubscribe}
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings}
import akka.cluster.{Cluster, ClusterEvent}
import com.oceanum.client.Implicits.DurationHelper
import com.oceanum.cluster.exec.RunnerManager
import com.oceanum.common.Scheduler.schedule
import com.oceanum.common._

import scala.collection.mutable

class MetricsListener extends Actor with ActorLogging {
  type Listeners = mutable.Map[ActorRef, (Long, Cancellable)]
  val cluster: Cluster = Cluster(context.system)
  val extension: ClusterMetricsExtension = ClusterMetricsExtension(context.system)
  val mediator: ActorRef = DistributedPubSub(context.system).mediator
  val metricListeners: Listeners = mutable.Map()
  val stateListeners: Listeners = mutable.Map()
  val taskInfoListeners: Listeners = mutable.Map()

  // Subscribe unto ClusterMetricsEvent events.
  override def preStart(): Unit = {
    extension.subscribe(self)
    mediator ! Subscribe(Environment.CLUSTER_NODE_METRICS_TOPIC, self)
    checkNodes(metricListeners)
    checkNodes(stateListeners)
    checkNodes(taskInfoListeners)
    cluster.subscribe(self, ClusterEvent.initialStateAsEvents, classOf[UnreachableMember], classOf[MemberEvent])
    context.become(receive(ClusterMetricsChanged(Set())))
  }

  private def checkNodes(nodes: mutable.Map[ActorRef, (Long, Cancellable)]): Unit = {
    val interval = fd"20s"
    schedule(interval, interval) {
      for (actor <- nodes.keys) {
        val (time, cancellable) = nodes(actor)
        if (System.currentTimeMillis() - time > fd"100s".toMillis) {
          nodes.remove(actor)
          cancellable.cancel()
        }
        actor ! Ping
      }
    }
  }
 
  // Unsubscribe from ClusterMetricsEvent events.
  override def postStop(): Unit = {
    extension.unsubscribe(self)
    mediator ! Unsubscribe(Environment.CLUSTER_NODE_METRICS_TOPIC, self)
  }
 
  def receive(clusterMetricsChanged: ClusterMetricsChanged): Receive = {

    case m: ClusterMetricsChanged ⇒
      context.become(receive(m))

    case taskInfo: NodeTaskInfoResponse =>
      log.info(taskInfo.toString)
      for (actor <- taskInfoListeners.keys) {
        actor ! taskInfo
      }

    case ClusterInfoMessageHolder(message: ClusterMessage, actor: ActorRef) => message match {

      case ClusterMetricsRequest(initialDelay, interval) =>
        val hook = schedule(fd"$initialDelay", fd"$interval") {
          self ! ClusterInfoMessageHolder(ClusterMetricsRequest, actor)
        }
        metricListeners.put(actor, (System.currentTimeMillis(), hook))

      case ClusterMetricsStopRequest(handler) =>
        actor ! removeScheduleNodes(handler, metricListeners)

      case ClusterMetricsRequest =>
        actor ! ClusterMetricsResponse(clusterMetricsChanged.nodeMetrics)

      case ClusterStateRequest(initialDelay, interval) =>
        val hook = schedule(fd"$initialDelay", fd"$interval") {
          self ! ClusterInfoMessageHolder(ClusterStateRequest, actor)
        }
        stateListeners.put(actor, (System.currentTimeMillis(), hook))

      case ClusterStateStopRequest(handler) =>
        actor ! removeScheduleNodes(handler, stateListeners)

      case ClusterStateRequest =>
        actor ! ClusterStateResponse(cluster.state)

      case NodeTaskInfoRequest(initialDelay, interval) =>
        log.info(NodeTaskInfoRequest(initialDelay, interval).toString)
        val hook = schedule(fd"$initialDelay", fd"$interval") {
          self ! ClusterInfoMessageHolder(NodeTaskInfoRequest, actor)
        }
        taskInfoListeners.put(actor, (System.currentTimeMillis(), hook))

      case NodeTaskInfoStopRequest(handler) =>
        log.info(NodeTaskInfoStopRequest(handler).toString)
        actor ! removeScheduleNodes(handler, taskInfoListeners)

      case NodeTaskInfoRequest =>
        log.info(NodeTaskInfoRequest.toString)
//        actor ! RunnerManager.getTaskInfo // TODO
    }

    case _: MemberEvent | UnreachableMember =>
      for (actor <- stateListeners.keys) {
        actor ! ClusterStateResponse(cluster.state)
      }

    case Pong =>
      updateIfExist(metricListeners)
      updateIfExist(stateListeners)
      updateIfExist(taskInfoListeners)

    case unknown =>
      println("unknown: " + unknown)
  }

  private def removeScheduleNodes(actorRef: ActorRef, listeners: Listeners): Boolean = {
    listeners.remove(actorRef) match {
      case Some((_, cancellable)) =>
        cancellable.cancel()
        true
      case None =>
        false
    }
  }

  private def updateIfExist(listeners: Listeners): Unit = {
    val actorRef = sender()
    if (listeners.contains(actorRef)) {
      listeners.put(actorRef, listeners(actorRef).copy(_1 = System.currentTimeMillis()))
    }
  }

  def receive: Receive = {
    case _ =>
  }
}

object MetricsListener {
  def start(): Unit = {
    val system = Environment.CLUSTER_NODE_SYSTEM
    system.actorOf(ClusterSingletonManager.props(
      singletonProps = Props(classOf[MetricsListener]),
      settings = ClusterSingletonManagerSettings(system),
      terminationMessage = PoisonPill.getInstance
    ), name = Environment.CLUSTER_NODE_METRICS_NAME)
  }
}