package com.oceanum.api

import akka.actor.ActorRef
import akka.util.Timeout
import com.oceanum.api.impl.TaskInstanceImpl
import com.oceanum.cluster.StateHandler
import com.oceanum.exec.State.State

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

/**
 * @author chenmingkun
 * @date 2020/6/18
 */
trait TaskInstance {
  def kill(): Future[Unit]

  def handleState(handler: StateHandler): Future[Unit]

  def handleState(interval: FiniteDuration, handler: State => Unit): Future[Unit]

  def close(): Future[Unit]

  def onComplete(handler: StateHandler): Future[Unit]

  def onComplete(handler: State => Unit): Future[Unit]

  def size: Int

  def isEmpty: Boolean
}

object TaskInstance {
  def apply(executor: TraversableOnce[ActorRef])(implicit timeout: Timeout = 10 second, executionContext: ExecutionContext = ExecutionContext.global): TaskInstance = {
    new TaskInstanceImpl(executor)
  }
}
