package com.oceanum.api

import com.oceanum.exec.{EventListener, Operator, OperatorTask}
import Implicits.MetadataHelper

@SerialVersionUID(22222200L)
case class Task(name: String,
                retryCount: Int,
                retryInterval: String,
                priority: Int,
                prop: TaskProp,
                metadata: Map[String, String] = Map.empty) {
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