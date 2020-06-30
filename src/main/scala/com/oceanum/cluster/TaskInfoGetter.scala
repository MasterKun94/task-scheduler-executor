package com.oceanum.cluster

import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import com.oceanum.cluster.exec.RunnerManager.{getTaskInfo, system}
import com.oceanum.common.{Environment, Scheduler}

/**
 * @author chenmingkun
 * @date 2020/7/1
 */
object TaskInfoGetter {
  import com.oceanum.client.Implicits.DurationHelper
  private val mediator = DistributedPubSub(system).mediator
  private val cancellable = Scheduler.schedule(fd"5s", fd"5s") { // TODO
    mediator ! Publish(Environment.CLUSTER_NODE_METRICS_TOPIC, getTaskInfo)
  }

  def trigger(): Unit = mediator ! Publish(Environment.CLUSTER_NODE_METRICS_TOPIC, getTaskInfo)

  def start(): Unit = cancellable

  def close(): Boolean = cancellable.cancel()
}
