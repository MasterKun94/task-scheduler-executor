package com.oceanum.common

import java.io.File
import java.util.Date

import com.oceanum.client.Task
import com.oceanum.common.Implicits.PathHelper
import com.oceanum.exec.{State, StdHandler}

@SerialVersionUID(1L)
class RichTaskMeta(id: Int = -1,
                   taskType: String = null,
                   user: String = null,
                   createTime: Date = null,
                   startTime: Date = null,
                   endTime: Date = null,
                   execDir: String = "",
                   message: String = "",
                   error: Throwable = null,
                   state: State.value = State.OFFLINE,
                   retryNum: Int = 0) extends TaskMeta(
  id = id,
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
           taskType: String = taskType,
           user: String = user,
           createTime: Date = createTime,
           startTime: Date = startTime,
           endTime: Date = endTime,
           execDir: String = execDir,
           message: String = message,
           error: Throwable = error,
           state: State.value = state,
           retryNum: Int = retryNum): RichTaskMeta = {
    new RichTaskMeta(
      id = id,
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

  def stdoutPath: String = outputPath/s"$id-stdout.out"

  def stderrPath: String = outputPath/s"$id-stderr.out"

  private lazy val outputPath: String = {
    //创建文件路径//创建文件路径
    val file: File = (execDir/"out").toFile
    //判断文件父目录是否已经存在,不存在则创建
    if (!file.exists)
      file.mkdirs
    file.getAbsolutePath
  }

  def stdoutHandler: StdHandler = Environment.CLUSTER_NODE_RUNNER_STDOUT_HANDLER_CLASS
    .getConstructor(this.getClass)
    .newInstance(this)
    .asInstanceOf[StdHandler]

  def stderrHandler: StdHandler = Environment.CLUSTER_NODE_RUNNER_STDERR_HANDLER_CLASS
    .getConstructor(this.getClass)
    .newInstance(this)
    .asInstanceOf[StdHandler]

  def withTask(task: Task): RichTaskMeta = {
    val graphMeta = task.rawEnv.graphMeta
    val dateFormat = DateUtil.format("yyyyMMdd").format(graphMeta.startTime)
    this.copy(
      id = task.id,
      taskType = task.prop.taskType,
      user = task.user,
      execDir = Environment.EXEC_WORK_DIR/dateFormat/graphMeta.name/graphMeta.id/task.id
    )
  }

  def failure(task: Task, e: Throwable): RichTaskMeta = {
    this.copy(
      id = task.id,
      taskType = task.prop.taskType,
      user = task.user,
      error = e,
      message = e.getMessage
    )
  }

  def update(meta: RichTaskMeta): RichTaskMeta = meta

  override def toString: String = s"TaskMeta(id=$id, taskType=$taskType, user=$user, createTime=$createTime, startTime=$startTime, endTime=$endTime, execDir=$execDir, message=$message, error=$error, state=$state, retryNum=$retryNum"
}


