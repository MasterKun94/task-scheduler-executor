package com.oceanum.api.impl

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.oceanum.api.TaskInstance
import com.oceanum.cluster.{CheckStateScheduled, HandleOnComplete, KillAction, StateHandler, TerminateAction}
import com.oceanum.exec.State.State

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

/**
 * @author chenmingkun
 * @date 2020/5/4
 */
class TaskInstanceImpl(executor: TraversableOnce[ActorRef])(implicit timeout: Timeout, executionContext: ExecutionContext)
extends TaskInstance {

  override def kill(): Future[Unit] = {
    Future.sequence(executor.map(client => client ? KillAction)) map (_ => Unit) mapTo
  }

  override def handleState(handler: StateHandler): Future[Unit] = {
    Future.sequence(executor.map(client => client ? CheckStateScheduled(handler.checkInterval(), handler))) map (_ => Unit) mapTo
  }

  override def handleState(interval: FiniteDuration, handler: State => Unit): Future[Unit] = {
    val stateHandler = new StateHandler {
      override def handle(state: State): Unit = handler(state)

      override def checkInterval(): FiniteDuration = interval
    }
    handleState(stateHandler)
  }

  override def close(): Future[Unit] = {
    Future.sequence(executor.map(client => client ? TerminateAction)) map (_ => Unit) mapTo
  }

  override def onComplete(handler: StateHandler): Future[Unit] = {
    Future.sequence(executor.map(client => client ? HandleOnComplete(handler))) map (_ => Unit) mapTo
  }

  override def onComplete(handler: State => Unit): Future[Unit] = {
    onComplete(StateHandler(handler))
  }

  override def size: Int = executor.size

  override def isEmpty: Boolean = executor.isEmpty
}