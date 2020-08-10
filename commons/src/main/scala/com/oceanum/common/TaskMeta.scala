package com.oceanum.common

import java.util.Date

import com.oceanum.annotation.ISerializationMessage

@SerialVersionUID(1L)
@ISerializationMessage("TASK_META")
class TaskMeta(val id: Int,
               val name: String,
               val rerunId: Int,
               val taskType: String,
               val user: String,
               val createTime: Option[Date],
               val startTime: Option[Date],
               val endTime: Option[Date],
               val execDir: String,
               val message: String,
               val error: Option[Throwable],
               val state: TaskStatus,
               val retryNum: Int,
               val host: String,
               val extendedProperties: Map[String, String]) extends Serializable
