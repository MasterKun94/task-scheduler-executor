package com.oceanum.cluster

import akka.actor.{Actor, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe, Unsubscribe}
import com.oceanum.exec.RunnerManager
import com.oceanum.exec.RunnerManager.getTaskInfo
import com.oceanum.common._

/**
 * @author chenmingkun
 * @date 2020/5/3
 */
class ExecutionEndpoint extends Actor {

  //使用pub/sub方式设置
  private val topics = Environment.CLUSTER_NODE_TOPICS
  private val mediator: ActorRef = Environment.CLUSTER_NODE_MEDIATOR

  override def preStart(): Unit = {
    for (topic <- topics) {
      mediator ! Subscribe(topic, self)
    }
    val duration = Environment.TASK_INFO_TRIGGER_INTERVAL
    Scheduler.schedule(duration, duration) {
      mediator ! Publish(Environment.CLUSTER_NODE_METRICS_TOPIC, getTaskInfo)
    }
  }

  override def postStop(): Unit = {
    for (topic <- topics) {
      mediator ! Unsubscribe(topic, self)
    }
  }

  override def receive: Receive = {
    case ExecuteOperatorRequest(task) =>
      context.system.actorOf(Props(classOf[ExecutionInstance], task, sender()))

    case _: AvailableExecutorRequest =>
      sender() ! AvailableExecutor(self, RunnerManager.getTaskInfo, Environment.CLUSTER_NODE_TOPICS)
  }
}