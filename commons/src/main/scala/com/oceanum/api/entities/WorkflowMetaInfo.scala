package com.oceanum.api.entities

import java.util.Date

import com.oceanum.annotation.ISerializationMessage
import com.oceanum.common._
import com.oceanum.persistence.MetaIdFactory

@ISerializationMessage("GRAPH_META_INFO")
case class WorkflowMetaInfo(id: Int,
                            name: String,
                            reRunId: Int,
                            tasks: Array[String],
                            latestTaskId: Int,
                            fallbackStrategy: FallbackStrategy.value,
                            reRunStrategy: ReRunStrategy.value,
                            graphStatus: GraphStatus.value,
                            error: Throwable,
                            createTime: Date,
                            scheduleTime: Date,
                            startTime: Date,
                            endTime: Date,
                            env: Map[String, Any]) {

  def toMeta(taskMetas: Seq[TaskMeta]): RichGraphMeta = new RichGraphMeta (
    id = id,
    name = name,
    reRunId = reRunId,
    tasks = taskMetas.map(m => (m.id, RichTaskMeta(m))).toMap,
    latestTaskId = latestTaskId,
    fallbackStrategy = fallbackStrategy,
    reRunStrategy = reRunStrategy,
    graphStatus = graphStatus,
    error = error,
    createTime = createTime,
    scheduleTime = scheduleTime,
    startTime = startTime,
    endTime = endTime,
    env = env
  )
}

object WorkflowMetaInfo {
  def from(graphMeta: GraphMeta): WorkflowMetaInfo = WorkflowMetaInfo (
    id = graphMeta.id,
    name = graphMeta.name,
    reRunId = graphMeta.reRunId,
    tasks = graphMeta.tasks.values.map(meta => MetaIdFactory.getTaskMetaId(graphMeta, meta)).toArray,
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