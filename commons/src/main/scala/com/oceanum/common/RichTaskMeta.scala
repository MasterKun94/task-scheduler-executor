package com.oceanum.common

import java.util.Date

import com.oceanum.annotation.ISerializationMessage
import com.oceanum.client.Task
import com.oceanum.common.Implicits.PathHelper

@SerialVersionUID(1L)
@ISerializationMessage("RICH_TASK_META")
sealed class RichTaskMeta(id: Int = -1,
                   name: String = null,
                   reRunId: Int = 0,
                   taskType: String = null,
                   user: String = null,
                   createTime: Date = null,
                   startTime: Date = null,
                   endTime: Date = null,
                   execDir: String = "",
                   message: String = "",
                   error: Throwable = null,
                   state: TaskStatus.value = TaskStatus.OFFLINE,
                   retryNum: Int = 0) extends TaskMeta(
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
) {

  def copy(id: Int = id,
           name: String = name,
           reRunId: Int = reRunId,
           taskType: String = taskType,
           user: String = user,
           createTime: Date = createTime,
           startTime: Date = startTime,
           endTime: Date = endTime,
           execDir: String = execDir,
           message: String = message,
           error: Throwable = error,
           state: TaskStatus.value = state,
           retryNum: Int = retryNum): RichTaskMeta = {
    new RichTaskMeta(
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

  def incRetry(): RichTaskMeta = this.copy(retryNum = this.retryNum + 1)

  def withTask(task: Task): RichTaskMeta = {
    val graphMeta = task.rawEnv.graphMeta
    val dateFormat = DateUtil.format("yyyyMMdd").format(graphMeta.startTime)
    this.copy(
      id = task.id,
      name = task.name,
      taskType = task.prop.taskType,
      user = task.user,
      execDir = Environment.EXEC_WORK_DIR/dateFormat/graphMeta.name/graphMeta.id/graphMeta.reRunId/task.id
    )
  }

  def failure(task: Task, e: Throwable): RichTaskMeta = {
    this.copy(
      id = task.id,
      name = task.name,
      taskType = task.prop.taskType,
      user = task.user,
      error = e,
      message = e.getMessage,
      state = TaskStatus.FAILED
    )
  }

  def update(meta: RichTaskMeta): RichTaskMeta = meta

  override def toString: String = s"TaskMeta(id=$id, taskType=$taskType, user=$user, createTime=$createTime, startTime=$startTime, endTime=$endTime, execDir=$execDir, message=$message, error=$error, state=$state, retryNum=$retryNum"
}

object RichTaskMeta {
  def apply(taskMeta: TaskMeta): RichTaskMeta = {
    taskMeta match {
      case richTaskMeta: RichTaskMeta => richTaskMeta
      case _ => new RichTaskMeta(
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
  }
}
