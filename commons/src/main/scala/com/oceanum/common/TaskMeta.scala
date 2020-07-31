package com.oceanum.common

import java.util.Date

import com.oceanum.annotation.ISerializationMessage

@SerialVersionUID(1L)
@ISerializationMessage("TASK_META")
class TaskMeta(val id: Int,
               val name: String,
               val reRunId: Int,
               val taskType: String,
               val user: String,
               val createTime: Date,
               val startTime: Date,
               val endTime: Date,
               val execDir: String,
               val message: String,
               val error: Throwable,
               val state: TaskStatus.value,
               val retryNum: Int) extends Serializable
