package com.oceanum.cluster

import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import com.oceanum.common.{Environment, Log}
import com.oceanum.exec.RunnerManager

/**
 * @author chenmingkun
 * @date 2020/7/1
 */
object TaskInfoTrigger extends Log {
  private lazy val mediator = DistributedPubSub(system).mediator

  def trigger(): Unit = mediator ! Publish(Environment.CLUSTER_NODE_METRICS_TOPIC, RunnerManager.getTaskInfo)
}
