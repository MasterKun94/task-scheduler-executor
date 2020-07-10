package com.oceanum.client

import com.oceanum.cluster.exec.{EventListener, Operator, OperatorTask}
import com.oceanum.common.Environment

import scala.concurrent.{ExecutionContext, Future}

@SerialVersionUID(1L)
case class Task(id: String,
                topic: String = Environment.EXEC_DEFAULT_TOPIC,
                user: String = Environment.EXEC_DEFAULT_USER,
                retryCount: Int = Environment.EXEC_DEFAULT_RETRY_MAX,
                retryInterval: String = Environment.EXEC_DEFAULT_RETRY_INTERVAL,
                priority: Int = Environment.EXEC_DEFAULT_PRIORITY,
                prop: TaskProp,
                stateHandler: StateHandler = StateHandler.default(),
                private val meta: TaskMeta = TaskMeta.empty) {
  def init(implicit listener: TaskMeta => EventListener, executor: ExecutionContext): Future[Operator[_ <: OperatorTask]] = {
    val task = metadata.lazyInit(this)
    val taskMeta = task.metadata
    task
      .prop
      .init(taskMeta)
      .map(ot => Operator(
        name = id,
        retryCount = retryCount,
        retryInterval = retryInterval,
        priority = priority,
        prop = ot,
        eventListener = listener(taskMeta),
        metadata = taskMeta
      ))
  }

  def metadata: TaskMeta = meta.withTask(this)
}

object Task {
  val builder: TaskBuilder.type = TaskBuilder
}