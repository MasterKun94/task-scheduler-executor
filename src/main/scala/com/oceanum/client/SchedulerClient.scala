package com.oceanum.client

import akka.util.Timeout
import com.oceanum.ShutdownHook
import com.oceanum.client.impl.SchedulerClientImpl
import com.oceanum.common.{ClusterMetricsResponse, ClusterStateResponse, Environment, NodeTaskInfoResponse}

import scala.concurrent.Future

/**
 * @author chenmingkun
 * @date 2020/5/4
 */
trait SchedulerClient {
  def execute(topic: String,
              task: Task,
              stateHandler: StateHandler = StateHandler.empty()): Future[TaskInstance]

  def executeAll(topic: String,
                 task: Task,
                 stateHandler: StateHandler = StateHandler.empty(), timeWait: String = "3s"): Future[TaskInstance]

  def handleClusterMetrics(interval: String)(handler: ClusterMetricsResponse => Unit): ShutdownHook

  def handleClusterInfo(interval: String)(handler: ClusterStateResponse => Unit): ShutdownHook

  def handleTaskInfo(interval: String)(handler: NodeTaskInfoResponse => Unit): ShutdownHook
}

object SchedulerClient {

  def apply(host: String, port: Int, seedNodes: String)(implicit timeout: Timeout): SchedulerClient = {
    SchedulerClientImpl(host, port, seedNodes)
  }

  def terminate(): Unit = Environment.CLIENT_SYSTEM.terminate()
}
