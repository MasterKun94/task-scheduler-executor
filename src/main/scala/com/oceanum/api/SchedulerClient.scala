package com.oceanum.api

import akka.util.Timeout
import com.oceanum.api.impl.SchedulerClientImpl
import com.oceanum.cluster.StateHandler

import scala.concurrent.{ExecutionContext, Future}

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
                 stateHandler: StateHandler = StateHandler.empty())(implicit timeWait: String = "1s"): Future[TaskInstance]
}

object SchedulerClient {

  def create(host: String, port: Int, seedNodes: String)(implicit executionContext: ExecutionContext, timeout: Timeout): SchedulerClient = {

    SchedulerClientImpl.create(host, port, seedNodes)
  }

  def terminate(): Unit = SchedulerClientImpl.clientSystem.terminate()
}
