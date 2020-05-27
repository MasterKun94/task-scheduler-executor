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
class ExecutionEndpoint(implicit scheduleExecutionContext: ExecutionContext) extends Actor {

  //使用pub/sub方式设置
  val mediator: ActorRef = DistributedPubSub(context.system).mediator

  override def preStart(): Unit = {
    for (topic <- Environment.CLUSTER_NODE_TOPICS) {
      mediator ! Subscribe(topic, self)
    }
  }

  override def receive: Receive = {
    case req: ExecuteOperatorRequest =>
      val executor = context.system.actorOf(Props(classOf[ExecutionActor], scheduleExecutionContext), "executor")
      executor.tell(req, sender())


    case AvailableExecutorRequest(_) =>
      sender() ! AvailableExecutor(self, ExecuteManager.queueSize, Environment.CLUSTER_NODE_TOPICS)
  }
}