package com.oceanum.metrics

import java.util.Date

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable, PoisonPill, Props}
import akka.cluster.Cluster
import akka.cluster.metrics.protobuf.msg.ClusterMetricsMessages
import akka.cluster.metrics.{ClusterMetricsChanged, ClusterMetricsExtension}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, Unsubscribe}
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings}
import com.oceanum.client.Implicits.DurationHelper
import com.oceanum.common.Scheduler.schedule
import com.oceanum.common._

import scala.collection.mutable
import scala.concurrent.ExecutionContext

class MetricsListener extends Actor with ActorLogging {
  val cluster: Cluster = Cluster(context.system)
  val extension: ClusterMetricsExtension = ClusterMetricsExtension(context.system)
  val mediator: ActorRef = DistributedPubSub(context.system).mediator
  var clusterMetricsChanged: ClusterMetricsChanged = _
  val scheduleNodes: mutable.Map[ActorRef, (Long, Cancellable)] = mutable.Map()

  // Subscribe unto ClusterMetricsEvent events.
  override def preStart(): Unit = {
    extension.subscribe(self)
    mediator ! Subscribe(Environment.CLUSTER_NODE_METRICS_TOPIC, self)
    schedule(fd"20s", fd"20s") {
      for (actor <- scheduleNodes.keys) {
        val (time, cancellable) = scheduleNodes(actor)
        if (System.currentTimeMillis() - time > fd"100s".toMillis) {
          scheduleNodes.remove(actor)
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
 
  def receive: Receive = {
    case m: ClusterMetricsChanged ⇒
      clusterMetricsChanged = m

    case ClusterInfoMessageHolder(message: ClusterInfoMessage, actor: ActorRef) => message match {

      case ClusterMetricsRequest(initialDelay, interval) =>
        val hook = schedule(fd"$initialDelay", fd"$interval") {
          self.tell(ClusterInfoMessageHolder(ClusterMetricsRequest, actor), actor)
        }
        scheduleNodes.put(actor, (System.currentTimeMillis(), hook))

      case ClusterMetricsStopRequest(handler) =>
        actor ! removeScheduleNodes(handler)

      case ClusterMetricsRequest =>
        actor ! ClusterMetricsResponse(clusterMetricsChanged.nodeMetrics)

      case ClusterInfoRequest(initialDelay, interval) =>
        val hook = schedule(fd"$initialDelay", fd"$interval") {
          self.tell(ClusterInfoMessageHolder(ClusterInfoRequest, actor), actor)
        }
        scheduleNodes.put(actor, (System.currentTimeMillis(), hook))

      case ClusterInfoStopRequest(handler) =>
        actor ! removeScheduleNodes(handler)

      case ClusterInfoRequest =>
        actor ! ClusterInfoResponse(cluster.state)
    }

    case Pong =>
      scheduleNodes.put(sender(), scheduleNodes(sender()).copy(_1 = System.currentTimeMillis()))

    case unknown =>
      println("unknown: " + unknown)
  }

  private def removeScheduleNodes(actorRef: ActorRef): Boolean = {
    scheduleNodes.remove(actorRef) match {
      case Some((_, cancellable)) =>
        cancellable.cancel()
        true
      case None =>
        false
    }
  }
}

object MetricsListener {
  def start(): Unit = {
    val system = Environment.CLUSTER_NODE_SYSTEM
    system.actorOf(ClusterSingletonManager.props(
      singletonProps = Props(classOf[MetricsListener]),
      settings = ClusterSingletonManagerSettings(system),
      terminationMessage = PoisonPill.getInstance
    ), name = "metrics-listener")
  }
}