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
                        rerunId: Int,
                        taskType: String,
                        user: String,
                        createTime: Option[Date],
                        startTime: Option[Date],
                        endTime: Option[Date],
                        execDir: String,
                        message: String,
                        error: Option[Throwable],
                        state: TaskStatus,
                        retryNum: Int,
                        host: String,
                        extendedProperties: Map[String, String] = Map.empty) {
  def toMeta: TaskMeta = new TaskMeta(
    id = id,
    name = name,
    rerunId = rerunId,
    taskType = taskType,
    user = user,
    createTime = createTime,
    startTime = startTime,
    endTime = endTime,
    execDir = execDir,
    message = message,
    error = error,
    state = state,
    retryNum = retryNum,
    host = host,
    extendedProperties = extendedProperties
  )
}

object TaskMetaInfo {
  def from(taskMeta: TaskMeta): TaskMetaInfo = TaskMetaInfo(
    id = taskMeta.id,
    name = taskMeta.name,
    rerunId = taskMeta.rerunId,
    taskType = taskMeta.taskType,
    user = taskMeta.user,
    createTime = taskMeta.createTime,
    startTime = taskMeta.startTime,
    endTime = taskMeta.endTime,
    execDir = taskMeta.execDir,
    message = taskMeta.message,
    error = taskMeta.error,
    state = taskMeta.state,
    retryNum = taskMeta.retryNum,
    host = taskMeta.host,
    extendedProperties = taskMeta.extendedProperties
  )
}
