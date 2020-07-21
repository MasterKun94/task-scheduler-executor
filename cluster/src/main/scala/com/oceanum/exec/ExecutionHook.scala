package com.oceanum.exec

import akka.actor.Cancellable

/**
 * @author chenmingkun
 * @date 2020/5/1
 */
trait ExecutionHook {
  def kill(): Boolean

  def isKilled: Boolean
}

object ExecutionHook {

  def apply(cancellable: Cancellable): ExecutionHook = new ExecutionHook {
    override def kill(): Boolean = cancellable.cancel()

    override def isKilled: Boolean = cancellable.isCancelled
  }
}
