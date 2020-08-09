package com.oceanum.common

import java.util.Date

import com.oceanum.annotation.ISerializationMessage

@SerialVersionUID(1L)
@ISerializationMessage("GRAPH_META")
class GraphMeta(val id: Int,
                val name: String,
                val reRunId: Int,
                val tasks: Map[Int, TaskMeta],
                val latestTaskId: Int,
                val fallbackStrategy: FallbackStrategy,
                val reRunStrategy: ReRunStrategy,
                val graphStatus: GraphStatus,
                val error: Option[Throwable],
                val createTime: Option[Date],
                val scheduleTime: Option[Date],
                val startTime: Option[Date],
                val endTime: Option[Date],
                val env: Map[String, Any]) extends Serializable {

  def latestTask: Option[TaskMeta] = tasks.get(latestTaskId)

  override def toString: String = s"GraphMeta(id=$id, name=$name, reRunId=$reRunId, tasks=$tasks, latestTask=$latestTaskId fallbackStrategy=$fallbackStrategy, reRunStrategy=$reRunStrategy, graphStatus=$graphStatus, error=$error, createTime=$createTime, scheduleTime=$scheduleTime, startTime=$startTime, endTime=$endTime, env=$env"
}
