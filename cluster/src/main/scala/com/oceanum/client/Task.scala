package com.oceanum.client

import java.util.Date

import com.oceanum.exec.{EventListener, ExecutionTask, TaskConfig}
import com.oceanum.common.{Environment, ExprContext}
import com.oceanum.graph.RichGraphMeta

@SerialVersionUID(1L)
case class Task(id: Int,
                topic: String = Environment.EXEC_DEFAULT_TOPIC,
                user: String = Environment.EXEC_DEFAULT_USER,
                retryCount: Int = Environment.EXEC_DEFAULT_RETRY_MAX,
                retryInterval: String = Environment.EXEC_DEFAULT_RETRY_INTERVAL,
                priority: Int = Environment.EXEC_DEFAULT_PRIORITY,
                checkStateInterval: String = Environment.EXEC_STATE_UPDATE_INTERVAL,
                prop: TaskProp,
                parallelism: Int = Environment.GRAPH_FLOW_DEFAULT_PARALLELISM,
                rawEnv: ExprContext = ExprContext.empty) {
  def toExecutionTask(implicit listener: EventListener): ExecutionTask[_ <: TaskConfig] = {
    val taskMeta = this.metadata.copy(createTime = new Date())
    ExecutionTask(
      name = "task" + id,
      retryCount = retryCount,
      retryInterval = retryInterval,
      priority = priority,
      prop = prop.toTask(taskMeta),
      eventListener = listener,
      env = env + taskMeta
    )
  }

  def validate(): Unit = {
    prop.validate()
  }

  def addGraphMeta(graphMeta: RichGraphMeta): Task = {
    this.copy(rawEnv = rawEnv + graphMeta)
  }

  def env: ExprContext = rawEnv + (ExprContext.taskKey -> metadata)

  private def metadata: RichTaskMeta = rawEnv.taskMeta.withTask(this)
}

object Task {
  val builder: TaskBuilder.type = TaskBuilder
}