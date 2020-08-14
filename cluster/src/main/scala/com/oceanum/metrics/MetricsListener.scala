package com.oceanum.metrics

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Cancellable, PoisonPill, Props}
import akka.cluster.ClusterEvent.{MemberEvent, UnreachableMember}
import akka.cluster.metrics.{ClusterMetricsChanged, ClusterMetricsExtension}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, SubscribeAck, Unsubscribe}
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings}
import akka.cluster.{Cluster, ClusterEvent}
import com.oceanum.api.entities.NodeTaskInfo
import com.oceanum.common.Scheduler.schedule
import com.oceanum.common._

import scala.collection.mutable

class MetricsListener extends Actor with ActorLogging {
  type Listeners = mutable.Map[ActorRef, (Long, Cancellable)]
  val cluster: Cluster = Cluster(context.system)
  val extension: ClusterMetricsExtension = ClusterMetricsExtension(context.system)
  val mediator: ActorRef = DistributedPubSub(context.system).mediator

  // Subscribe unto ClusterMetricsEvent events.
  override def preStart(): Unit = {
    extension.subscribe(self)
    mediator ! Subscribe(Environment.CLUSTER_NODE_METRICS_TOPIC, self)
  }
 
  // Unsubscribe from ClusterMetricsEvent events.
  override def postStop(): Unit = {
    extension.unsubscribe(self)
    mediator ! Unsubscribe(Environment.CLUSTER_NODE_METRICS_TOPIC, self)
  }

  def receive: Receive = {
    case _ => // TODO
  }
}

object MetricsListener {

  def start(system: ActorSystem): Unit = {
    system.actorOf(ClusterSingletonManager.props(
      singletonProps = Props(classOf[MetricsListener]),
      settings = ClusterSingletonManagerSettings(system),
      terminationMessage = PoisonPill.getInstance
    ), name = Environment.CLUSTER_NODE_METRICS_NAME)
  }
}