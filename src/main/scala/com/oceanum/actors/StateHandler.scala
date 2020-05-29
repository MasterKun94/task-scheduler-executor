package com.oceanum.actors

import com.oceanum.exec.EventListener
import com.oceanum.exec.EventListener.State

import scala.concurrent.duration.FiniteDuration

/**
 * @author chenmingkun
 * @date 2020/5/8
 */
trait StateHandler extends Serializable {
  def apply(state: EventListener.State): Unit = handle(state)

  def handle(state: EventListener.State): Unit

  def checkInterval(): FiniteDuration = FiniteDuration(10, "s")
}

object StateHandler {
  def apply(handler: EventListener.State => Unit): StateHandler = new StateHandler() {
    override def handle(state: State): Unit = handler(state)
  }

  def empty(): StateHandler = new StateHandler {
    override def handle(state: State): Unit = Unit
  }
}
