package com.oceanum.persistence.es

import java.util.Date

import com.oceanum.annotation.ISerializationMessage
import com.oceanum.common.{FallbackStrategy, GraphStatus, ReRunStrategy}

@ISerializationMessage("ES_GRAPH_META")
class EsGraphMeta(val id: Int,
                  val name: String,
                  val reRunId: Int,
                  val tasks: Array[String],
                  val latestTaskId: String,
                  val fallbackStrategy: FallbackStrategy.value,
                  val reRunStrategy: ReRunStrategy.value,
                  val graphStatus: GraphStatus.value,
                  val error: Throwable,
                  val createTime: Date,
                  val scheduleTime: Date,
                  val startTime: Date,
                  val endTime: Date,
                  val env: Map[String, Any]) {
  def getEsId: String = s""
}

