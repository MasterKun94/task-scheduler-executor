package com.oceanum.client.actors

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.cluster.client.ClusterClient.Publish
import com.oceanum.common._

/**
 * @author chenmingkun
 * @date 2020/5/3
 */
class ClientEndpoint(clusterClient: ActorRef) extends Actor with ActorLogging {

  override def receive: Receive = {
    case req: AvailableExecutorsRequest =>
      clusterClient ! Publish(req.topic, AvailableExecutorRequest(req.topic))
      context.become(receiveExecutors(sender(), Array.empty))
      Scheduler.scheduleOnce(req.maxWait) {
        self ! req
      }

    case req: AvailableExecutorRequest =>
      clusterClient ! Publish(req.topic, req)
      context.become(receiveExecutor(sender()))

    case req: ClusterMessage =>
      clusterClient ! Publish(Environment.CLUSTER_NODE_METRICS_TOPIC, ClusterInfoMessageHolder(req, sender()))
  }

  def receiveExecutors(receiver: ActorRef, executors: Array[AvailableExecutor]): Receive = {
    case executor: AvailableExecutor =>
      context.become(receiveExecutors(receiver, executors :+ executor))

    case _: AvailableExecutorsRequest =>
      receiver ! AvailableExecutorResponse(executors)
      context.stop(self)
  }

  def receiveExecutor(receiver: ActorRef): Receive = {
    case executor: AvailableExecutor =>
      println(executor)
      receiver ! AvailableExecutorResponse(Some(executor))
      context.stop(self)
  }
}