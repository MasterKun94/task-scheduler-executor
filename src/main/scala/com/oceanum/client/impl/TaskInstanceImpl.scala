package com.oceanum.client.impl

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.oceanum.client.{StateHandler, TaskInstance}
import com.oceanum.cluster.exec.State.State
import com.oceanum.common.{HandleOnComplete, KillAction, TerminateAction}

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
    Future.sequence(executor.map(client => client ? handler)) map (_ => Unit) mapTo
  }

  override def handleState(interval: String, handler: State => Unit): Future[Unit] = {
    val stateHandler = new StateHandler {
      override def handle(state: State): Unit = handler(state)

      override def checkInterval(): String = interval
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