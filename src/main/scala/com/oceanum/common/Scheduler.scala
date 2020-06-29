package com.oceanum.common

import akka.actor.{ActorContext, ActorRefFactory, ActorSystem, Cancellable}

import scala.concurrent.ExecutionContext
import com.oceanum.client.Implicits.DurationHelper

import scala.concurrent.duration.FiniteDuration

/**
 * @author chenmingkun
 * @date 2020/6/29
 */
object Scheduler {
  implicit val ec: ExecutionContext = Environment.SCHEDULE_EXECUTION_CONTEXT

  def schedule(initialDelay: FiniteDuration, interval: FiniteDuration)(f: => Unit)(implicit context: ActorContext): Cancellable = {
    val system = context match {
      case sys: ActorSystem => sys
      case ctx: ActorContext => ctx.system
    }
    system.scheduler.schedule(initialDelay, interval)(f)
  }

  def scheduleOnce(initialDelay: FiniteDuration)(f: => Unit)(implicit context: ActorRefFactory): Cancellable = {
    val system = context match {
      case sys: ActorSystem => sys
      case ctx: ActorContext => ctx.system
    }
    system.scheduler.scheduleOnce(initialDelay)(f)
  }
}
