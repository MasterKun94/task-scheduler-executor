package com.oceanum.cluster.exec

import java.util.concurrent.atomic.{AtomicBoolean, AtomicReference}

import com.oceanum.client.TaskMeta

/**
 * @author chenmingkun
 * @date 2020/4/29
 */
case class Operator[T <: OperatorTask](name: String,
                                       retryCount: Int,
                                       retryInterval: String,
                                       priority: Int,
                                       prop: T,
                                       eventListener: EventListener,
                                       metadata: TaskMeta,
                                       private val hookRef: AtomicReference[Hook] = new AtomicReference(),
                                       private val ref: AtomicBoolean = new AtomicBoolean(false)
                                       ) {
  val hook: Hook = new Hook {
    override def kill(): Boolean = {
      ref.set(true)
      if (hookRef.get() != null) hookRef.get().kill() else false
    }

    override def isKilled: Boolean = {
      ref.get()
    }
  }

  def receive(hook: Hook): Unit = {
    hookRef.set(hook)
    if (this.hook.isKilled) {
      hook.kill()
    }
  }

  def retry(): Operator[T] = this.copy(retryCount = this.retryCount - 1)
}
