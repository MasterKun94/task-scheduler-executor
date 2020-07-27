package com.oceanum.exec

import java.util.concurrent.atomic.{AtomicBoolean, AtomicReference}

import com.oceanum.common.{GraphContext, RichTaskMeta}

import scala.concurrent.{ExecutionContext, Future}

/**
 * @author chenmingkun
 * @date 2020/4/29
 */
case class ExecutionTask[T <: TaskConfig](name: String,
                                          retryCount: Int,
                                          retryInterval: String,
                                          priority: Int,
                                          prop: T,
                                          eventListener: EventListener,
                                          env: GraphContext,
                                          private val hookRef: AtomicReference[ExecutionHook] = new AtomicReference(),
                                          private val ref: AtomicBoolean = new AtomicBoolean(false)
                                       ) {
  val hook: ExecutionHook = new ExecutionHook {
    override def kill(): Boolean = {
      ref.set(true)
      if (hookRef.get() != null) hookRef.get().kill() else false
    }

    override def isKilled: Boolean = {
      ref.get()
    }
  }

  def receive(hook: ExecutionHook): Unit = {
    hookRef.set(hook)
    if (this.hook.isKilled) {
      hook.kill()
    }
  }
  def retry(): ExecutionTask[T] = {
    val meta = metadata.incRetry()
    this.copy(retryCount = this.retryCount - 1).updateMeta(meta)
  }

  def metadata: RichTaskMeta = env.taskMeta

  def prepareStart(implicit ec: ExecutionContext): Future[ExecutionTask[_<:TaskConfig]] = {
    prop.prepare(env)
      .map(p => this.copy(prop = p))
  }

  def updateMeta(meta: RichTaskMeta): ExecutionTask[T] = {
    this.copy(env = env.copy(taskMeta = meta))
  }
}
