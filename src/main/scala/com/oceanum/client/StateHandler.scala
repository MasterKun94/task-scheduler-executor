package com.oceanum.client

import com.oceanum.cluster.exec.State

/**
 * @author chenmingkun
 * @date 2020/5/8
 */
trait StateHandler extends Serializable {

  def handle(state: State): Unit

  def checkInterval(): String = "10s"
}

object StateHandler {
  def apply(handler: State => Unit): StateHandler = new StateHandler() {
    override def handle(state: State): Unit = handler(state)
  }

  def empty(): StateHandler = new StateHandler {
    override def handle(state: State): Unit = Unit
  }
}
