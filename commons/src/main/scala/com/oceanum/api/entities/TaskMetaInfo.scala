package com.oceanum.api.entities

import java.util.Date

import com.oceanum.annotation.ISerializationMessage
import com.oceanum.common.{TaskMeta, TaskStatus}

/**
 * @author chenmingkun
 * @date 2020/8/1
 */
@ISerializationMessage("TASK_META_INFO")
case class TaskMetaInfo(id: Int,
                        name: String,
                        reRunId: Int,
                        taskType: String,
                        user: String,
                        createTime: Date,
                        startTime: Date,
                        endTime: Date,
                        execDir: String,
                        message: String,
                        error: Throwable,
                        state: TaskStatus.value,
                        retryNum: Int) {
  def toMeta: TaskMeta = new TaskMeta(
    id = id,
    name = name,
    reRunId = reRunId,
    taskType = taskType,
    user = user,
    createTime = createTime,
    startTime = startTime,
    endTime = endTime,
    execDir = execDir,
    message = message,
    error = error,
    state = state,
    retryNum = retryNum
  )
}

object TaskMetaInfo {
  def from(taskMeta: TaskMeta): TaskMetaInfo = TaskMetaInfo(
    id = taskMeta.id,
    name = taskMeta.name,
    reRunId = taskMeta.reRunId,
    taskType = taskMeta.taskType,
    user = taskMeta.user,
    createTime = taskMeta.createTime,
    startTime = taskMeta.startTime,
    endTime = taskMeta.endTime,
    execDir = taskMeta.execDir,
    message = taskMeta.message,
    error = taskMeta.error,
    state = taskMeta.state,
    retryNum = taskMeta.retryNum
  )
}
