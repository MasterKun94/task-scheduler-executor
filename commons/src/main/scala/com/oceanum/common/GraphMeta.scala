package com.oceanum.common

import java.util.Date

import com.oceanum.annotation.ISerializationMessage

@SerialVersionUID(1L)
@ISerializationMessage("GRAPH_META")
class GraphMeta(val id: Int,
                val name: String,
                val rerunId: Int,
                val tasks: Map[Int, TaskMeta],
                val latestTaskId: Int,
                val fallbackStrategy: FallbackStrategy,
                val rerunStrategy: RerunStrategy,
                val graphStatus: GraphStatus,
                val error: Option[Throwable],
                val createTime: Option[Date],
                val scheduleTime: Option[Date],
                val startTime: Option[Date],
                val endTime: Option[Date],
                val env: Map[String, Any],
                val host: String) extends Serializable {

  def latestTask: Option[TaskMeta] = tasks.get(latestTaskId)

  override def toString: String = s"GraphMeta(id=$id, name=$name, rerunId=$rerunId, tasks=$tasks, latestTask=$latestTaskId fallbackStrategy=$fallbackStrategy, rerunStrategy=$rerunStrategy, graphStatus=$graphStatus, error=$error, createTime=$createTime, scheduleTime=$scheduleTime, startTime=$startTime, endTime=$endTime, env=$env"
}
