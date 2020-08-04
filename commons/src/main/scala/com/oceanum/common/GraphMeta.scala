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
                val fallbackStrategy: FallbackStrategy.value,
                val reRunStrategy: ReRunStrategy.value,
                val graphStatus: GraphStatus.value,
                val error: Throwable,
                val createTime: Date,
                val scheduleTime: Date,
                val startTime: Date,
                val endTime: Date,
                val env: Map[String, Any],
                val host: String = Environment.HOST) extends Serializable {

  def latestTask: TaskMeta = if (latestTaskId < 0) null else tasks(latestTaskId)

  override def toString: String = s"GraphMeta(id=$id, name=$name, reRunId=$reRunId, tasks=$tasks, latestTask=$latestTaskId fallbackStrategy=$fallbackStrategy, reRunStrategy=$reRunStrategy, graphStatus=$graphStatus, error=$error, createTime=$createTime, scheduleTime=$scheduleTime, startTime=$startTime, endTime=$endTime, env=$env"
}
