package com.oceanum.graph

import java.util.Date

import com.oceanum.client.TaskMeta

class NewGraphMeta(val id: Int,
                   val name: String,
                   val operators: Map[Int, TaskMeta[_]],
                   val fallbackStrategy: FallbackStrategy.value,
                   val reRunStrategy: ReRunStrategy.value,
                   val graphStatus: GraphStatus.value,
                   val error: Throwable,
                   val createTime: Date,
                   val scheduleTime: Date,
                   val startTime: Date,
                   val endTime: Date,
                   val env: Map[String, Any])
