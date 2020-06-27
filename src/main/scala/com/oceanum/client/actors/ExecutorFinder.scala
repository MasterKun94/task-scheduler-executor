package com.oceanum.client.actors

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.cluster.client.ClusterClient.Publish
import com.oceanum.client.Implicits._
import com.oceanum.common.{AvailableExecutor, AvailableExecutorRequest, AvailableExecutorResponse, AvailableExecutorsRequest}

import scala.concurrent.ExecutionContext

/**
 * @author chenmingkun
 * @date 2020/5/3
 */
class ExecutorFinder(clusterClient: ActorRef, executionContext: ExecutionContext) extends Actor with ActorLogging {
  implicit private val ec: ExecutionContext = executionContext

  override def receive: Receive = {
    case req: AvailableExecutorsRequest =>
      clusterClient ! Publish(req.topic, AvailableExecutorRequest(req.topic))
      context.become(receiveExecutors(sender(), Array.empty))
      context.system.scheduler.scheduleOnce(fd"${req.maxWait}") {
        self ! req
      }

    case req: AvailableExecutorRequest =>
      clusterClient ! Publish(req.topic, req)
      context.become(receiveExecutor(sender()))
  }

  def receiveExecutors(receiver: ActorRef, executors: Array[AvailableExecutor]): Receive = {
    case executor: AvailableExecutor =>
      context.become(receiveExecutors(receiver, executors :+ executor))

    case req: AvailableExecutorsRequest =>
      receiver ! AvailableExecutorResponse(executors)
      context.stop(self)
  }

  def receiveExecutor(receiver: ActorRef): Receive = {
    case executor: AvailableExecutor =>
      receiver ! AvailableExecutorResponse(Some(executor))
      context.stop(self)
  }
}