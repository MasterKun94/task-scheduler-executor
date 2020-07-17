package com.oceanum.client

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

import com.oceanum.cluster.exec.{State, StdHandler}
import com.oceanum.common.{Environment, Meta}
import com.oceanum.common.Implicits.PathHelper


class RichTaskMeta(map: Map[String, Any]) extends Meta[RichTaskMeta](map) with TaskMeta[RichTaskMeta] {

  override def id: Int = this("id")

  override def taskType: String = this("taskType")

  override def user: String = this("user")

  override def createTime: Date = this("createTime")

  def createTime_=(date: Date): RichTaskMeta = this + ("createTime" -> date)

  override def startTime: Date = this("startTime")

  def startTime_=(date: Date): RichTaskMeta = this + ("startTime" -> date)

  override def endTime: Date = this("endTime")

  def endTime_=(date: Date): RichTaskMeta = this + ("endTime" -> date)

  override def execDir: String = this("execDir")

  def message_=(message: String):RichTaskMeta = this + ("message" -> message)

  override def message: String = this("message")

  def error_=(e: Throwable): RichTaskMeta = this + ("error" -> e) + ("message" -> e.getMessage)

  override def error: Throwable = this("error")

  def state_=(state: State.value): RichTaskMeta = this + ("state" -> state)

  override def state: State.value = this("this")

  def lazyInit_=(func: Task => Task): RichTaskMeta = this + ("lazyInit" -> func)

  def lazyInit: Task => Task = this.get[Task => Task ]("lazyInit")
    .map(_.andThen(task => task.copy(meta = task.metadata - "lazyInit")))
    .getOrElse(l => l)

  override def retryNum: Int = this.get("retryNum").getOrElse(0)

  def incRetry(): RichTaskMeta = this + ("retryNum" -> (this.retryNum + 1))

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
    val dateFormat = new SimpleDateFormat("yyyyMMdd").format(System.currentTimeMillis())
    this ++ RichTaskMeta(
      "id" -> task.id,
      "taskType" -> task.prop.taskType,
      "user" -> task.user,
      "createTime" -> System.currentTimeMillis().toString,
      "execDir" -> Environment.EXEC_WORK_DIR/dateFormat/task.user/task.id
    )
  }

  override def toString: String = s"TaskMeta(${map.toArray.map(t => t._1 + ": " + t._2).mkString(", ")})"

}

object RichTaskMeta {

  def empty: RichTaskMeta = new RichTaskMeta(Map.empty)

  def apply(kv: (String, Any)*): RichTaskMeta = new RichTaskMeta(Map(kv: _*))
}
