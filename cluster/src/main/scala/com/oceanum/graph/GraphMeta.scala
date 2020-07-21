package com.oceanum.graph

import java.util.Date

import com.oceanum.client.TaskMeta

@SerialVersionUID(1L)
class GraphMeta(val id: Int,
                val name: String,
                val operators: Map[Int, TaskMeta],
                val fallbackStrategy: FallbackStrategy.value,
                val reRunStrategy: ReRunStrategy.value,
                val graphStatus: GraphStatus.value,
                val error: Throwable,
                val createTime: Date,
                val scheduleTime: Date,
                val startTime: Date,
                val endTime: Date,
                val env: Map[String, Any]) extends Serializable {
  override def toString: String = s"GraphMeta(id=$id, name=$name, operators=$operators, fallbackStrategy=$fallbackStrategy, reRunStrategy=$reRunStrategy, graphStatus=$graphStatus, error=$error, createTime=$createTime, scheduleTime=$scheduleTime, startTime=$startTime, endTime=$endTime, env=$env"
}

object GraphMeta {
  def toJson(graphMeta: GraphMeta): String = ???

  def fromJson(json: String): String = ???
}
