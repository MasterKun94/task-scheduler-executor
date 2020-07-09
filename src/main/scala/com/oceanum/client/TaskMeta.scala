package com.oceanum.client

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

import com.oceanum.cluster.exec.InputStreamHandler
import com.oceanum.common.{Environment, Meta}
import com.oceanum.common.Implicits.PathHelper


class TaskMeta(map: Map[String, Any]) extends Meta[TaskMeta](map) {

  def id: String = this("id")

  def taskType: String = this("taskType")

  def user: String = this("user")

  def createTime: Date = this("createTime")

  def createTime_=(date: Date): TaskMeta = this + ("createTime" -> date)

  def startTime: Date = this("startTime")

  def startTime_=(date: Date): TaskMeta = this + ("startTime" -> date)

  def endTime: Date = this("endTime")

  def endTime_=(date: Date): TaskMeta = this + ("endTime" -> date)

  def execDir: String = this("execDir")

  def lazyInit_=(func: Task => Task): TaskMeta = this + ("lazyInit" -> func)

  def lazyInit(task: Task): Task = this.get[Task => Task]("lazyInit") match {
    case Some(f) => f(task).copy(meta = task.metadata - "lazyInit")
    case None => task
  }

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

  def stdoutHandler: InputStreamHandler = Environment.CLUSTER_NODE_RUNNER_STDOUT_HANDLER_CLASS
    .getConstructor(this.getClass)
    .newInstance(this)
    .asInstanceOf[InputStreamHandler]

  def stderrHandler: InputStreamHandler = Environment.CLUSTER_NODE_RUNNER_STDERR_HANDLER_CLASS
    .getConstructor(this.getClass)
    .newInstance(this)
    .asInstanceOf[InputStreamHandler]

  def withTask(task: Task): TaskMeta = {
    val dateFormat = new SimpleDateFormat("yyyyMMdd").format(System.currentTimeMillis())
    this ++ TaskMeta(
      "id" -> task.id,
      "taskType" -> task.prop.taskType,
      "user" -> task.user,
      "createTime" -> System.currentTimeMillis().toString,
      "execDir" -> Environment.EXEC_WORK_DIR/dateFormat/task.user/task.id
    )
  }

  override def toString: String = s"Metadata(${map.toArray.map(t => t._1 + ": " + t._2).mkString(", ")})"
}

object TaskMeta {

  def empty: TaskMeta = new TaskMeta(Map.empty)

  def apply(kv: (String, Any)*): TaskMeta = new TaskMeta(Map(kv: _*))
}
