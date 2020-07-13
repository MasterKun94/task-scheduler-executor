package com.oceanum.cluster

import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import com.oceanum.cluster.exec.RunnerManager.getTaskInfo
import com.oceanum.common.{Environment, Log}

/**
 * @author chenmingkun
 * @date 2020/7/1
 */
object TaskInfoTrigger extends Log {
  private lazy val mediator = Environment.CLUSTER_NODE_MEDIATOR

  def trigger(): Unit = mediator ! Publish(Environment.CLUSTER_NODE_METRICS_TOPIC, getTaskInfo)
}
