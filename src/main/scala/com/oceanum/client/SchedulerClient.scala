package com.oceanum.client

import akka.util.Timeout
import com.oceanum.client.impl.SchedulerClientImpl
import com.oceanum.common.Environment

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
                 stateHandler: StateHandler = StateHandler.empty(), timeWait: String = "3s"): Future[TaskInstance]
}

object SchedulerClient {

  def create(host: String, port: Int, seedNodes: String)(implicit timeout: Timeout): SchedulerClient = {

    SchedulerClientImpl.create(host, port, seedNodes)
  }

  def terminate(): Unit = Environment.CLIENT_SYSTEM.terminate()
}
