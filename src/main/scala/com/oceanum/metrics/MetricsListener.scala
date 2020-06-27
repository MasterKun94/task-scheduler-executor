package com.oceanum.metrics

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.CurrentClusterState
import akka.cluster.metrics.StandardMetrics.{Cpu, HeapMemory}
import akka.cluster.metrics.{ClusterMetricsChanged, ClusterMetricsExtension, NodeMetrics}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, Unsubscribe}
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings}
import com.oceanum.common.Environment

import scala.concurrent.duration.FiniteDuration

class MetricsListener extends Actor with ActorLogging {
  val cluster: Cluster = Cluster(context.system)
  val extension: ClusterMetricsExtension = ClusterMetricsExtension(context.system)
  val mediator: ActorRef = DistributedPubSub(context.system).mediator

  // Subscribe unto ClusterMetricsEvent events.
  override def preStart(): Unit = {
    extension.subscribe(self)
    import scala.concurrent.ExecutionContext.Implicits.global
    context.system.scheduler.schedule(FiniteDuration(5L, "s"), FiniteDuration(5L, "s")) {
      println(cluster.state)
    }
    mediator ! Subscribe(Environment.CLUSTER_NODE_METRICS_TOPIC, self)
  }
 
  // Unsubscribe from ClusterMetricsEvent events.
  override def postStop(): Unit = {
    extension.unsubscribe(self)
    mediator ! Unsubscribe(Environment.CLUSTER_NODE_METRICS_TOPIC, self)
  }
 
  def receive = {

    case ClusterMetricsChanged(clusterMetrics) ⇒
      clusterMetrics.flatMap(f => f.metrics).foreach(m => println("metric: " + m))
      println()

    case state: CurrentClusterState ⇒ println("state: " + state)

    case unknown => println("unknown: " + unknown)
  }
 
  def logHeap(nodeMetrics: NodeMetrics): Unit = nodeMetrics match {
    case HeapMemory(address, timestamp, used, committed, max) ⇒
      log.info("Used heap: {} MB", used.doubleValue / 1024 / 1024)
    case Cpu(address, timestamp, Some(systemLoadAverage), cpuCombined, cpuStolen, processors) ⇒
      log.info("Load: {} ({} processors)", systemLoadAverage, processors)
    case m ⇒
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