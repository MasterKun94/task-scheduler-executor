package com.oceanum.common

import java.util.Date

import com.oceanum.exec.State

@SerialVersionUID(1L)
class TaskMeta(val id: Int,
               val name: String = null,
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
