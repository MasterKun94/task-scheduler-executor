package com.oceanum.api.entities

import java.util.Date

import com.oceanum.annotation.ISerializationMessage
import com.oceanum.common._
import com.oceanum.persistence.MetaIdFactory

@ISerializationMessage("GRAPH_META_INFO")
case class WorkflowMetaInfo(id: Int,
                            name: String,
                            rerunId: Int,
                            tasks: Array[String],
                            latestTaskId: Int,
                            fallbackStrategy: FallbackStrategy,
                            rerunStrategy: RerunStrategy,
                            graphStatus: GraphStatus,
                            error: Option[Throwable],
                            createTime: Option[Date],
                            scheduleTime: Option[Date],
                            startTime: Option[Date],
                            endTime: Option[Date],
                            env: Map[String, Any],
                            host: String) {

  def toMeta(taskMetas: Seq[TaskMeta]): RichGraphMeta = new RichGraphMeta (
    id = id,
    name = name,
    rerunId = rerunId,
    tasks = taskMetas.map(m => (m.id, RichTaskMeta(m))).toMap,
    latestTaskId = latestTaskId,
    fallbackStrategy = fallbackStrategy,
    rerunStrategy = rerunStrategy,
    graphStatus = graphStatus,
    error = error,
    createTime = createTime,
    scheduleTime = scheduleTime,
    startTime = startTime,
    endTime = endTime,
    env = env,
    host = host
  )
}

object WorkflowMetaInfo {
  def from(graphMeta: GraphMeta): WorkflowMetaInfo = WorkflowMetaInfo (
    id = graphMeta.id,
    name = graphMeta.name,
    rerunId = graphMeta.rerunId,
    tasks = graphMeta.tasks.values.map(meta => MetaIdFactory.getTaskMetaId(graphMeta, meta)).toArray,
    latestTaskId = graphMeta.latestTaskId,
    fallbackStrategy = graphMeta.fallbackStrategy,
    rerunStrategy = graphMeta.rerunStrategy,
    graphStatus = graphMeta.graphStatus,
    error = graphMeta.error,
    createTime = graphMeta.createTime,
    scheduleTime = graphMeta.scheduleTime,
    startTime = graphMeta.startTime,
    endTime = graphMeta.endTime,
    env = graphMeta.env,
    host = graphMeta.host
  )
}