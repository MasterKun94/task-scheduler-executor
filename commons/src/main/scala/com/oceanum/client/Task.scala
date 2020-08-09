package com.oceanum.client

import java.util.UUID

import com.oceanum.common.{Environment, GraphContext, RichGraphMeta, RichTaskMeta}

@SerialVersionUID(1L)
case class Task(id: Int = -1,
                name: String = UUID.randomUUID().toString,
                topic: String = Environment.EXEC_DEFAULT_TOPIC,
                user: String = Environment.EXEC_DEFAULT_USER,
                retryCount: Int = Environment.EXEC_DEFAULT_RETRY_MAX,
                retryInterval: String = Environment.EXEC_DEFAULT_RETRY_INTERVAL,
                priority: Int = Environment.EXEC_DEFAULT_PRIORITY,
                checkStateInterval: String = Environment.EXEC_STATE_UPDATE_INTERVAL,
                prop: TaskProp = null,
                parallelism: Int = Environment.GRAPH_FLOW_DEFAULT_PARALLELISM,
                rawEnv: GraphContext = GraphContext.empty) {

  def validate(): Unit = {
    prop.validate()
  }

  def addGraphMeta(graphMeta: RichGraphMeta): Task = {
    this.copy(rawEnv = rawEnv.copy(graphMeta = graphMeta))
  }

  def env: GraphContext = rawEnv.copy(taskMeta = taskMeta)

  private def taskMeta: RichTaskMeta = {
    if (rawEnv.taskMeta == null) RichTaskMeta(this)
    else rawEnv.taskMeta.asInstanceOf[RichTaskMeta]
  }
}

object Task {
  val builder: TaskBuilder.type = TaskBuilder
}