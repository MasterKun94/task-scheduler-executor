package com.oceanum.client

import java.util.UUID

import com.oceanum.cluster.exec.{EventListener, Operator, OperatorTask}
import com.oceanum.common.Implicits.MetadataHelper

@SerialVersionUID(22222200L)
case class Task(name: String = "",
                topic: String = "",
                retryCount: Int = 1,
                retryInterval: String = "3 minute",
                priority: Int = 5,
                prop: TaskProp,
                metadata: Metadata = Metadata.empty) {
  def toOperator(listener: EventListener): Operator[_ <: OperatorTask] = Operator(
    name,
    retryCount,
    retryInterval,
    priority,
    prop.toTask(metadata.withTask(this)),
    listener)
}

object Task {
  val builder: TaskBuilder.type = TaskBuilder
}