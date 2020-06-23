package com.oceanum.api

import com.oceanum.exec.{EventListener, Operator, OperatorTask}

@SerialVersionUID(22222200L)
case class Task(name: String, retryCount: Int, retryInterval: Int, priority: Int, prop: TaskProp) {
  def toOperator(listener: EventListener): Operator[_ <: OperatorTask] = Operator(name, retryCount, retryInterval, priority, prop.toTask, listener)
}