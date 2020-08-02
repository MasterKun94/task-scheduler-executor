package com.oceanum.api.entities

import com.oceanum.annotation.ISerializationMessage
import com.oceanum.client.Task

/**
 * @author chenmingkun
 * @date 2020/8/2
 */
trait Vertex { }

@ISerializationMessage("TASK_VERTEX")
case class TaskVertex(task: Task, parallelism: Int = 1) extends Vertex

@ISerializationMessage("FORK_VERTEX")
case class ForkVertex(parallelism: Int) extends Vertex

@ISerializationMessage("JOIN_VERTEX")
case class JoinVertex(parallelism: Int) extends Vertex

@ISerializationMessage("DECISION_VERTEX")
case class DecisionVertex(expr: Array[String]) extends Vertex

@ISerializationMessage("CONVERGE_VERTEX")
case class ConvergeVertex(parallelism: Int) extends Vertex

@ISerializationMessage("START_VERTEX")
case class StartVertex() extends Vertex

@ISerializationMessage("END_VERTEX")
case class EndVertex() extends Vertex
