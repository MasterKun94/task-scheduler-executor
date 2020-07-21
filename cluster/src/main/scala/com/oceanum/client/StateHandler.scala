package com.oceanum.client

import com.oceanum.exec.State

/**
 * @author chenmingkun
 * @date 2020/5/8
 */
trait StateHandler {

  def handle(state: State): Unit
}

object StateHandler {
  def apply(handler: State => Unit): StateHandler = new StateHandler() {
    override def handle(state: State): Unit = handler(state)
  }

  def empty(): StateHandler = new StateHandler {
    override def handle(state: State): Unit = Unit
  }

  def default(): StateHandler = empty()
}
