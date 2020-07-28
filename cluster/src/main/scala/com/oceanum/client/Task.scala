package com.oceanum.client

import java.util.{Date, UUID}

import com.oceanum.common.{Environment, GraphContext, RichGraphMeta, RichTaskMeta}
import com.oceanum.exec.{EventListener, ExecutionTask, TaskConfig}

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
  def toExecutionTask(implicit listener: EventListener): ExecutionTask[_ <: TaskConfig] = {
    val taskMeta = this.metadata.copy(createTime = new Date())
    ExecutionTask(
      name = name,
      retryCount = retryCount,
      retryInterval = retryInterval,
      priority = priority,
      prop = prop.toTask(taskMeta),
      eventListener = listener,
      env = env.copy(taskMeta = taskMeta)
    )
  }

  def validate(): Unit = {
    prop.validate()
  }

  def addGraphMeta(graphMeta: RichGraphMeta): Task = {
    this.copy(rawEnv = rawEnv.copy(graphMeta = graphMeta))
  }

  def env: GraphContext = rawEnv + (GraphContext.taskKey -> metadata)

  private def metadata: RichTaskMeta = {
    if (rawEnv.taskMeta == null) new RichTaskMeta().withTask(this)
    else rawEnv.taskMeta
  }
}

object Task {
  val builder: TaskBuilder.type = TaskBuilder
}