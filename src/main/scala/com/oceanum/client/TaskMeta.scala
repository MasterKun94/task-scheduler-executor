package com.oceanum.client

import java.util.Date

import com.oceanum.cluster.exec.State

@SerialVersionUID(1L)
class TaskMeta(val id: Int,
               val taskType: String,
               val user: String,
               val createTime: Date,
               val startTime: Date,
               val endTime: Date,
               val execDir: String,
               val message: String,
               val error: Throwable,
               val state: State.value,
               val retryNum: Int) extends Serializable
