package com.oceanum.client

import com.oceanum.cluster.exec.{EventListener, Operator, OperatorTask}
import com.oceanum.common.Environment
import com.oceanum.common.Implicits.TaskMetadataHelper

import scala.concurrent.{ExecutionContext, Future}

@SerialVersionUID(1L)
case class Task(id: String,
                topic: String = Environment.EXEC_DEFAULT_TOPIC,
                user: String = Environment.EXEC_DEFAULT_USER,
                retryCount: Int = Environment.EXEC_DEFAULT_RETRY_MAX,
                retryInterval: String = Environment.EXEC_DEFAULT_RETRY_INTERVAL,
                priority: Int = Environment.EXEC_DEFAULT_PRIORITY,
                prop: TaskProp,
                private val meta: Metadata = Metadata.empty) {
  def init(listener: Metadata => EventListener)(implicit executor: ExecutionContext): Future[Operator[_ <: OperatorTask]] = {
    val task = metadata.lazyInit(this)
    println(task)
    task
      .prop
      .init(task.metadata)
      .map(ot => Operator(
        name = id,
        retryCount = retryCount,
        retryInterval = retryInterval,
        priority = priority,
        prop = ot,
        eventListener = listener(task.metadata),
        metadata = task.metadata
      ))
  }

  def metadata: Metadata = meta.withTask(this)
}

object Task {
  val builder: TaskBuilder.type = TaskBuilder
}