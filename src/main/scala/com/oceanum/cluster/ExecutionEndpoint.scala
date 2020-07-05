package com.oceanum.cluster

import akka.actor.{Actor, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe, Unsubscribe}
import com.oceanum.cluster.exec.RunnerManager
import com.oceanum.cluster.exec.RunnerManager.getTaskInfo
import com.oceanum.common._

/**
 * @author chenmingkun
 * @date 2020/5/3
 */
class ExecutionEndpoint extends Actor {

  //使用pub/sub方式设置
  private val topics = Environment.CLUSTER_NODE_TOPICS
  private val mediator: ActorRef = DistributedPubSub(context.system).mediator
  def trigger(): Unit = mediator ! Publish(Environment.CLUSTER_NODE_METRICS_TOPIC, getTaskInfo)

  override def preStart(): Unit = {
    for (topic <- topics) {
      mediator ! Subscribe(topic, self)
    }
    import Implicits.DurationHelper
    val duration = fd"${Environment.TASK_INFO_TRIGGER_INTERVAL}"
    Scheduler.schedule(duration, duration) {
      trigger()
    }
  }

  override def postStop(): Unit = {
    for (topic <- topics) {
      mediator ! Unsubscribe(topic, self)
    }
  }

  override def receive: Receive = {
    case req: ExecuteOperatorRequest =>
      val instance = context.system.actorOf(Props(classOf[ExecutionInstance]), "execution-instance")
      instance.tell(req, sender())


    case req: AvailableExecutorRequest =>
      sender() ! AvailableExecutor(self, RunnerManager.getTaskInfo, Environment.CLUSTER_NODE_TOPICS)
  }
}