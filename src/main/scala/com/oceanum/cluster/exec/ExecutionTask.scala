package com.oceanum.cluster.exec

import java.util.Date
import java.util.concurrent.atomic.{AtomicBoolean, AtomicReference}

import com.oceanum.client.RichTaskMeta

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
  def retry(): ExecutionTask[T] = this.copy(retryCount = this.retryCount - 1, metadata = metadata.incRetry())

  def prepareStart(implicit ec: ExecutionContext): Future[ExecutionTask[_<:TaskConfig]] = {
    import com.oceanum.common.Implicits.EnvHelper
    val meta = metadata.copy(startTime = new Date())
    this.prop.prepare(meta, env.addTask(meta))
      .map(p => this.copy(prop = p))
  }
}
