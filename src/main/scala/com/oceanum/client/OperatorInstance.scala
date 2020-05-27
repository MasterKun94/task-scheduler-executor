package com.oceanum.client

import akka.actor.ActorRef
import akka.util.Timeout
import com.oceanum.exec.EventListener
import akka.pattern.ask
import com.oceanum.actors.StateHandler

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.{FiniteDuration, _}
/**
 * @author chenmingkun
 * @date 2020/5/4
 */
case class OperatorInstance(client: ActorRef)(implicit timeout: Timeout = 10 second, executionContext: ExecutionContext = ExecutionContext.global) {

  def kill(): Future[Unit] = {
    client ? KillAction map (_ => Unit) mapTo
  }

  def checkStateScheduled(duration: FiniteDuration, handler: StateHandler): Future[Unit] = {
    client ? CheckStateScheduled(duration, handler) map (_ => Unit) mapTo
  }

  def close(): Future[Unit] = {
    client ? TerminateAction map (_ => Unit) mapTo
  }

  def onComplete(handler: StateHandler): Future[Unit] = {
    client ? HandleOnComplete(handler) map (_ => Unit) mapTo
  }

  def onComplete(handler: EventListener.State => Unit): Future[Unit] = {
    onComplete(StateHandler(handler))
  }
}