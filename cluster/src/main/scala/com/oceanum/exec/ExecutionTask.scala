package com.oceanum.exec

import java.util.Date
import java.util.concurrent.atomic.{AtomicBoolean, AtomicReference}

import com.oceanum.client.RichTaskMeta
import com.oceanum.common.Implicits.EnvHelper

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
                                          metadata: RichTaskMeta,
                                          env: Map[String, Any],
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

  def prepareStart(implicit ec: ExecutionContext): Future[ExecutionTask[_<:TaskConfig]] = {
    val meta = env.getTask.copy(startTime = new Date())
    val task = this.updateMeta(meta)
    task.prop.prepare(task.env)
      .map(p => this.copy(prop = p))
  }
  def updateMeta(meta: RichTaskMeta): ExecutionTask[T] = this.copy(env = env.addTask(meta), metadata = meta)
}
