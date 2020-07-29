package com.oceanum.common

import java.util.{Date, UUID}

import com.oceanum.exec.{FAILED, KILL, SUCCESS, State}

@SerialVersionUID(1L)
sealed class RichGraphMeta(id: Int = 0,
                    name: String = UUID.randomUUID().toString,
                    reRunId: Int = 0,
                    tasks: Map[Int, RichTaskMeta] = Map.empty,
                    latestTaskId: Int = -1,
                    fallbackStrategy: FallbackStrategy.value = FallbackStrategy.CONTINUE,
                    reRunStrategy: ReRunStrategy.value = ReRunStrategy.NONE,
                    graphStatus: GraphStatus.value = GraphStatus.OFFLINE,
                    error: Throwable = null,
                    createTime: Date = null,
                    scheduleTime: Date = null,
                    startTime: Date = null,
                    endTime: Date = null,
                    env: Map[String, Any] = Map.empty,
                    val reRunFlag: Boolean = false
                      )
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
           fallbackStrategy: FallbackStrategy.value = fallbackStrategy,
           reRunStrategy: ReRunStrategy.value = reRunStrategy,
           graphStatus: GraphStatus.value = graphStatus,
           error: Throwable = error,
           createTime: Date = createTime,
           scheduleTime: Date = scheduleTime,
           startTime: Date = startTime,
           endTime: Date = endTime,
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

  def updateGraphStatus(status: GraphStatus.value): RichGraphMeta = {
    this.copy(graphStatus = Seq(graphStatus, status).maxBy(_.id))
  }

  def merge(meta: GraphMeta): RichGraphMeta = {
    val keys: Set[Int] = this.tasks.keySet ++ meta.tasks.keySet
    val map: Map[Int, RichTaskMeta] = keys.map { key => {
      val task = (this.tasks.get(key), meta.tasks.get(key)) match {
        case (Some(o1), Some(o2)) =>
          if (o1.createTime.before(o2.createTime)) o2 else o1
        case (None, Some(o2)) => o2
        case (Some(o1), None) => o1
        case (None, None) => throw new IllegalArgumentException
      }
      (key, task.asInstanceOf[RichTaskMeta])
    }}.toMap
    val latest = Array(latestTask, meta.asInstanceOf[RichGraphMeta].latestTask)
      .map( t => {
        if (t == null) 0L -> -1
        else if (t.startTime == null) 0L -> t.id
        else t.startTime.getTime -> t.id
      })
        .maxBy(_._1)._2
    updateGraphStatus(meta.graphStatus).copy(tasks = map, latestTaskId = latest)
  }

  def end: RichGraphMeta = {
    val meta = graphStatus match {
      case GraphStatus.RUNNING => this.updateGraphStatus(GraphStatus.SUCCESS)

      case GraphStatus.EXCEPTION => this.updateGraphStatus(GraphStatus.FAILED)

      case _: GraphStatus.value => this
    }
    meta.copy(endTime = new Date())
  }

  def addEnv(kv: (String, Any)): RichGraphMeta = this.copy(env = this.env + kv)

  def addEnv(right: Map[String, Any]): RichGraphMeta = this.copy(env = this.env ++ right)
}

