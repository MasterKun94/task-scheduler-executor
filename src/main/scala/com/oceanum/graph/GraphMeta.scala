package com.oceanum.graph

import com.oceanum.client.TaskMeta
import com.oceanum.common.Meta

class GraphMeta(map: Map[String, Any]) extends Meta[GraphMeta](map) {
  def id: String = this("id")
  def listFinishedTasks: Seq[TaskMeta] = this("finishedTasks")
  def withFinishedTasks(taskMeta: TaskMeta): GraphMeta = this + ("finishedTasks" -> (this.listFinishedTasks :+ taskMeta))
}
