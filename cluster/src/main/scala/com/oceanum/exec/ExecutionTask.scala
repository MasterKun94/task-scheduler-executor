package com.oceanum.exec

import java.util.Date
import java.util.concurrent.atomic.{AtomicBoolean, AtomicReference}

import com.oceanum.client.Task
import com.oceanum.common.{Environment, GraphContext, RichTaskMeta}

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

  /**
   * 接收新的钩子
   */
  def receive(hook: ExecutionHook): Unit = {
    hookRef.set(hook)
    if (this.hook.isKilled) {
      hook.kill()
    }
  }

  /**
   * 任务重试
   *
   * @return 重试次数减一次后的任务配置
   */
  def retry(): ExecutionTask[T] = {
    val meta = metadata.incRetry()
    this.copy(retryCount = this.retryCount - 1).updateTaskMeta(meta)
  }

  def metadata: RichTaskMeta = RichTaskMeta(env.taskMeta)

  /**
   * 任务执行前准备
   *
   * @return 准备好的任务配置
   */
  def prepareStart(implicit ec: ExecutionContext): Future[ExecutionTask[_<:TaskConfig]] = {
    prop.prepare(env)
      .map(p => this.copy(prop = p))
  }

  def updateTaskMeta(meta: RichTaskMeta): ExecutionTask[T] = {
    this.copy(env = env.copy(taskMeta = meta))
  }
}

object ExecutionTask {
  def from(task: Task, listener: EventListener): ExecutionTask[_ <: TaskConfig] = {
    val env = task.env
    val taskMeta = RichTaskMeta(env.taskMeta).copy(createTime = Option(new Date()), host = Environment.HOST)
    ExecutionTask(
      name = task.name,
      retryCount = task.retryCount,
      retryInterval = task.retryInterval,
      priority = task.priority,
      prop = TaskConfig.from(task.prop, taskMeta, listener),
      eventListener = listener,
      env = env.copy(taskMeta = taskMeta)
    )
  }
}