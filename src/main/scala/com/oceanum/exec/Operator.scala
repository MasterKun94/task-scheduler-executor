package com.oceanum.exec

import java.util.concurrent.atomic.{AtomicBoolean, AtomicReference}

/**
 * @author chenmingkun
 * @date 2020/4/29
 */
case class Operator[T <: OperatorTask](name: String,
                                       retryCount: Int,
                                       retryInterval: Int,
                                       priority: Int,
                                       prop: T,
                                       eventListener: EventListener
                                       ) {
  private val hookRef = new AtomicReference[ExecutorHook]()
  private val ref: AtomicBoolean = new AtomicBoolean(false)
  protected[exec] val hook: ExecutorHook = new ExecutorHook {
    override def kill(): Boolean = {
      ref.set(true)
      if (hookRef.get() != null) hookRef.get().kill() else false
    }

    override def isKilled: Boolean = {
      ref.get()
    }
  }

  protected[exec] def receive(hook: ExecutorHook): Unit = {
    hookRef.set(hook)
    if (hook.isKilled) {
      hook.kill()
    }
  }
}
