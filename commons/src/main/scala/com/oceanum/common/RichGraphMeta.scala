package com.oceanum.common

import java.util.{Date, UUID}

import com.oceanum.annotation.ISerializationMessage

@SerialVersionUID(1L)
@ISerializationMessage("RICH_GRAPH_META")
sealed class RichGraphMeta(id: Int = -1,
                           name: String = UUID.randomUUID().toString,
                           reRunId: Int = 0,
                           tasks: Map[Int, RichTaskMeta] = Map.empty,
                           latestTaskId: Int = -1,
                           fallbackStrategy: FallbackStrategy = FallbackStrategy.CONTINUE,
                           reRunStrategy: ReRunStrategy = ReRunStrategy.NONE,
                           graphStatus: GraphStatus = GraphStatus.OFFLINE,
                           error: Option[Throwable] = None,
                           createTime: Option[Date] = None,
                           scheduleTime: Option[Date] = None,
                           startTime: Option[Date] = None,
                           endTime: Option[Date] = None,
                           env: Map[String, Any] = Map.empty,
                           reRunFlag: Boolean = false)
  extends GraphMeta(
    id = id,
    name = name,
    reRunId = reRunId,
    tasks = tasks,
    latestTaskId = latestTaskId,
    fallbackStrategy = fallbackStrategy,
    reRunStrategy = reRunStrategy,
    graphStatus = graphStatus,
    error = error,
    createTime = createTime,
    scheduleTime = scheduleTime,
    startTime = startTime,
    endTime = endTime,
    env = env) {

  def copy(id: Int = id,
           name: String = name,
           reRunId: Int = reRunId,
           tasks: Map[Int, RichTaskMeta] = tasks,
           latestTaskId: Int = latestTaskId,
           fallbackStrategy: FallbackStrategy = fallbackStrategy,
           reRunStrategy: ReRunStrategy = reRunStrategy,
           graphStatus: GraphStatus = graphStatus,
           error: Option[Throwable] = error,
           createTime: Option[Date] = createTime,
           scheduleTime: Option[Date] = scheduleTime,
           startTime: Option[Date] = startTime,
           endTime: Option[Date] = endTime,
           env: Map[String, Any] = env,
           reRunFlag: Boolean = reRunFlag): RichGraphMeta = {
    new RichGraphMeta(
      id = id,
      name = name,
      reRunId = reRunId,
      tasks = tasks,
      latestTaskId = latestTaskId,
      fallbackStrategy = fallbackStrategy,
      reRunStrategy = reRunStrategy,
      graphStatus = graphStatus,
      error = error,
      createTime = createTime,
      scheduleTime = scheduleTime,
      startTime = startTime,
      endTime = endTime,
      env = env,
      reRunFlag = reRunFlag)
  }

  def isReRun: Boolean = reRunFlag

  def addTask(taskMeta: RichTaskMeta, isComplete: Boolean = false): RichGraphMeta = {
    val graphStatus = taskMeta.state match {
      case TaskStatus.SUCCESS => GraphStatus.RUNNING
      case TaskStatus.FAILED => GraphStatus.EXCEPTION
      case TaskStatus.KILL => GraphStatus.KILLED
      case _ => GraphStatus.RUNNING
    }
    val tuple = taskMeta.id -> taskMeta
    if (isComplete)
      updateGraphStatus(graphStatus).copy(tasks = this.tasks + tuple, latestTaskId = taskMeta.id)
    else
      updateGraphStatus(graphStatus).copy(tasks = this.tasks + tuple)
  }

  def updateGraphStatus(status: GraphStatus): RichGraphMeta = {
    this.copy(graphStatus = Seq(graphStatus, status).maxBy(_.value))
  }

  def merge(meta: GraphMeta): RichGraphMeta = {
    val keys: Set[Int] = this.tasks.keySet ++ meta.tasks.keySet
    val map: Map[Int, RichTaskMeta] = keys.map { key => {
      val task = (this.tasks.get(key), meta.tasks.get(key)) match {
        case (Some(o1), Some(o2)) =>
          if (o1.createTime.get.before(o2.createTime.get)) o2 else o1
        case (None, Some(o2)) => o2
        case (Some(o1), None) => o1
        case (None, None) => throw new IllegalArgumentException
      }
      (key, task.asInstanceOf[RichTaskMeta])
    }}.toMap
    val completedTasks = map.filter(t => t._2.reRunId == meta.reRunId && t._2.endTime != null)
    val latest = if (completedTasks.isEmpty) -1 else completedTasks.maxBy(_._2.endTime)._2.id
    updateGraphStatus(meta.graphStatus).copy(tasks = map, latestTaskId = latest)
  }

  def end: RichGraphMeta = {
    val meta = graphStatus match {
      case GraphStatus.RUNNING => this.updateGraphStatus(GraphStatus.SUCCESS)

      case GraphStatus.EXCEPTION => this.updateGraphStatus(GraphStatus.FAILED)

      case _ => this
    }
    meta.copy(endTime = Option(new Date()))
  }

  def addEnv(kv: (String, Any)): RichGraphMeta = this.copy(env = this.env + kv)

  def addEnv(right: Map[String, Any]): RichGraphMeta = this.copy(env = this.env ++ right)
}

object RichGraphMeta {
  def apply(graphMeta: GraphMeta): RichGraphMeta = {
    graphMeta match {
      case meta: RichGraphMeta => meta
      case _ => new RichGraphMeta(
        id = graphMeta.id,
        name = graphMeta.name,
        reRunId = graphMeta.reRunId,
        tasks = graphMeta.tasks.mapValues(RichTaskMeta.apply),
        latestTaskId = graphMeta.latestTaskId,
        fallbackStrategy = graphMeta.fallbackStrategy,
        reRunStrategy = graphMeta.reRunStrategy,
        graphStatus = graphMeta.graphStatus,
        error = graphMeta.error,
        createTime = graphMeta.createTime,
        scheduleTime = graphMeta.scheduleTime,
        startTime = graphMeta.startTime,
        endTime = graphMeta.endTime,
        env = graphMeta.env
      )
    }
  }
}

