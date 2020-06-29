package com.oceanum.cluster.exec

import akka.actor.Cancellable

/**
 * @author chenmingkun
 * @date 2020/5/1
 */
trait Hook {
  def kill(): Boolean

  def isKilled: Boolean
}

object Hook {

  def apply(cancellable: Cancellable): Hook = new Hook {
    override def kill(): Boolean = cancellable.cancel()

    override def isKilled: Boolean = cancellable.isCancelled
  }
}
