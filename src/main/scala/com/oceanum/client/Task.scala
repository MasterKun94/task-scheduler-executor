package com.oceanum.client

import com.oceanum.cluster.exec.{EventListener, ExecutionTask, TaskConfig}
import com.oceanum.common.Environment
import com.oceanum.common.StringParser.parseExpr

import scala.concurrent.{ExecutionContext, Future}

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
                env: Map[String, Any] = Map.empty,
                private val meta: RichTaskMeta = RichTaskMeta.empty) {
  def init(implicit listener: RichTaskMeta => EventListener, executor: ExecutionContext): Future[ExecutionTask[_ <: TaskConfig]] = {
    val task = metadata.lazyInit(this).parse
    val taskMeta = task.metadata
    task
      .prop
      .init(taskMeta)
      .map(ot => ExecutionTask(
        name = "task" + id,
        retryCount = retryCount,
        retryInterval = retryInterval,
        priority = priority,
        prop = ot,
        eventListener = listener(taskMeta),
        metadata = taskMeta
      ))
  }

  def parse: Task = this.copy(
    user = parseExpr(user)(env),
    env = env.mapValues(f => if (f.isInstanceOf[String]) parseExpr(f.asInstanceOf)(env) else f),
    prop = prop.parse(env)
  )

  def metadata: RichTaskMeta = meta.withTask(this)
}

object Task {
  val builder: TaskBuilder.type = TaskBuilder
}