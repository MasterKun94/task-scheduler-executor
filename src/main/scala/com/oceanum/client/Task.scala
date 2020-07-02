package com.oceanum.client

import com.oceanum.cluster.exec.{EventListener, Operator, OperatorTask}
import com.oceanum.common.Implicits.TaskMetadataHelper

@SerialVersionUID(22222200L)
case class Task(id: String,
                topic: String = "default",
                user: String = "default",
                retryCount: Int = 1,
                retryInterval: String = "3 minute",
                priority: Int = 5,
                prop: TaskProp,
                private val meta: Metadata = Metadata.empty) {
  def toOperator(listener: EventListener): Operator[_ <: OperatorTask] = Operator(
    id,
    retryCount,
    retryInterval,
    priority,
    prop.toTask(metadata),
    listener)

  def metadata: Metadata = meta.withTask(this)
}

object Task {
  val builder: TaskBuilder.type = TaskBuilder
}