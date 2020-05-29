package com.oceanum.client

import akka.actor.ActorRef
import akka.util.Timeout
import com.oceanum.exec.EventListener
import akka.pattern.ask
import com.oceanum.actors.StateHandler
import com.oceanum.exec.EventListener.State

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.{FiniteDuration, _}
import scala.language.postfixOps
/**
 * @author chenmingkun
 * @date 2020/5/4
 */
case class ExecutorInstance(executor: TraversableOnce[ActorRef])(implicit timeout: Timeout = 10 second, executionContext: ExecutionContext = ExecutionContext.global) {

  def kill(): Future[Unit] = {
    Future.sequence(executor.map(client => client ? KillAction)) map (_ => Unit) mapTo
  }

  def handleState(handler: StateHandler): Future[Unit] = {
    Future.sequence(executor.map(client => client ? CheckStateScheduled(handler.checkInterval(), handler))) map (_ => Unit) mapTo
  }

  def handleState(interval: FiniteDuration, handler: EventListener.State => Unit): Future[Unit] = {
    val stateHandler = new StateHandler {
      override def handle(state: State): Unit = handler(state)

      override def checkInterval(): FiniteDuration = interval
    }
    handleState(stateHandler)
  }

  def close(): Future[Unit] = {
    Future.sequence(executor.map(client => client ? TerminateAction)) map (_ => Unit) mapTo
  }

  def onComplete(handler: StateHandler): Future[Unit] = {
    Future.sequence(executor.map(client => client ? HandleOnComplete(handler))) map (_ => Unit) mapTo
  }

  def onComplete(handler: EventListener.State => Unit): Future[Unit] = {
    onComplete(StateHandler(handler))
  }

  def size: Int = executor.size

  def isEmpty: Boolean = executor.isEmpty
}