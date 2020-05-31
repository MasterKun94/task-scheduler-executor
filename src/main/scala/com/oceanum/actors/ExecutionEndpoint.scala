package com.oceanum.actors

import akka.actor.{Actor, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Subscribe
import com.oceanum.client.{AvailableExecutor, AvailableExecutorRequest, ExecuteOperatorRequest}
import com.oceanum.common.Environment
import com.oceanum.exec.ExecuteManager

import scala.concurrent.ExecutionContext

/**
 * @author chenmingkun
 * @date 2020/5/3
 */
class ExecutionEndpoint(topics: Seq[String]) extends Actor {

  //使用pub/sub方式设置
  val mediator: ActorRef = DistributedPubSub(context.system).mediator

  override def preStart(): Unit = {
    for (topic <- topics) {
      mediator ! Subscribe(topic, self)
    }
  }

  override def receive: Receive = {
    case req: ExecuteOperatorRequest =>
      val proxy = context.system.actorOf(Props(classOf[ExecutionInstance]), "execution-proxy")
      proxy.tell(req, sender())


    case _: AvailableExecutorRequest =>
      sender() ! AvailableExecutor(self, ExecuteManager.queueSize, Environment.CLUSTER_NODE_TOPICS)
  }
}