package com.oceanum.client

import java.util.Date

import com.oceanum.cluster.exec.{EventListener, ExecutionTask, TaskConfig}
import com.oceanum.common.Environment
import com.oceanum.common.Implicits.EnvHelper
import com.oceanum.graph.{GraphMeta, RichGraphMeta}

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
                rawEnv: Map[String, Any] = Map.empty) {
  def toExecutionTask(implicit listener: RichTaskMeta => EventListener): ExecutionTask[_ <: TaskConfig] = {
    val task = this
    val taskMeta = task.metadata
    ExecutionTask(
      name = "task" + id,
      retryCount = retryCount,
      retryInterval = retryInterval,
      priority = priority,
      prop = prop.toTask(taskMeta),
      eventListener = listener(taskMeta),
      metadata = taskMeta.createTime = new Date(),
      env = env
    )
  }

  def validate: Unit = {
    prop.validate
  }

  def addGraphMeta(graphMeta: RichGraphMeta): Task = this.copy(rawEnv = rawEnv.combineGraph(graphMeta))

  def env: Map[String, Any] = rawEnv + (EnvHelper.taskKey -> metadata)

  private def metadata: RichTaskMeta = rawEnv.getTask.withTask(this)
}

object Task {
  val builder: TaskBuilder.type = TaskBuilder
}