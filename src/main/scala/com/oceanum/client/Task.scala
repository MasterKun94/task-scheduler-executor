package com.oceanum.client

import com.oceanum.cluster.exec.{EventListener, Operator, OperatorTask}
import Implicits.MetadataHelper

@SerialVersionUID(22222200L)
case class Task(name: String,
                retryCount: Int,
                retryInterval: String,
                priority: Int,
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

object TaskBuilder {
  def newTask(name: String): TaskBuilder = new TaskBuilder(Task(name = "", retryCount = 1, retryInterval = "1m", priority = 5, prop = null)) // TODO
}

class TaskBuilder(task: Task) {

}