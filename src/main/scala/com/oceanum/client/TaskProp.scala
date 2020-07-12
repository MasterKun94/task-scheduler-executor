package com.oceanum.client

import java.io.File

import com.oceanum.cluster.exec.TaskConfig
import com.oceanum.cluster.tasks.SysTasks.UserAddTaskConfig
import com.oceanum.cluster.tasks._
import com.oceanum.file.FileClient

import scala.concurrent.{ExecutionContext, Future}
import com.oceanum.common.Implicits.PathHelper

@SerialVersionUID(1L)
abstract class TaskProp(val taskType: String) extends Serializable {

  def init(metadata: TaskMeta)(implicit executor: ExecutionContext): Future[TaskConfig]
}

@SerialVersionUID(1L)
abstract class ProcessTaskProp(taskType: String) extends TaskProp(taskType) with Serializable {

  def files: Seq[String] = Seq.empty

  def toTask(metadata: TaskMeta, fileMap: Map[String, String]): ProcessTaskConfig

  override def init(metadata: TaskMeta)(implicit executor: ExecutionContext): Future[ProcessTaskConfig] = {
    val fileMap: Map[String, String] = files
      .map(src => (src, metadata.execDir/new File(src).getName))
      .toMap
    fileMap.map(kv => FileClient.download(kv._1, kv._2))
      .reduce((f1, f2) => f1.flatMap(_ => f2))
      .map(_ => toTask(metadata, fileMap))
      .map(task => SuUserTaskConfig(metadata.user, task))
  }
}

@SerialVersionUID(1L)
case class ShellTaskProp(cmd: Array[String] = Array.empty,
                         env: Map[String, String] = Map.empty,
                         directory: String = "",
                         waitForTimeout: Long = -1) extends ProcessTaskProp("SHELL") {
  override def toTask(metadata: TaskMeta, fileMap: Map[String, String]): ProcessTaskConfig = ShellTaskConfig(
    cmd, env, directory, waitForTimeout, metadata.stdoutHandler, metadata.stderrHandler)
}

@SerialVersionUID(1L)
case class ShellScriptTaskProp(scriptFile: String = "",
                               args: Array[String] = Array.empty,
                               env: Map[String, String] = Map.empty,
                               directory: String = "",
                               waitForTimeout: Long = -1) extends ProcessTaskProp("SHELL_SCRIPT") {
  override def files: Seq[String] = Seq(scriptFile)

  override def toTask(metadata: TaskMeta, fileMap: Map[String, String]): ProcessTaskConfig = ShellScriptTaskConfig(
    fileMap(scriptFile), args, env, directory, waitForTimeout, metadata.stdoutHandler, metadata.stderrHandler)
}

@SerialVersionUID(1L)
case class JavaTaskProp(jars: Array[String] = Array.empty,
                        mainClass: String = "",
                        args: Array[String] = Array.empty,
                        options: Array[String] = Array.empty,
                        env: Map[String, String] = Map.empty,
                        directory: String = "",
                        waitForTimeout: Long = -1) extends ProcessTaskProp("JAVA") {
  override def files: Seq[String] = jars.toSeq

  override def toTask(metadata: TaskMeta, fileMap: Map[String, String]): ProcessTaskConfig = JavaTaskConfig(
    jars.map(s => fileMap(s)), mainClass, args, options, env, directory, waitForTimeout, metadata.stdoutHandler, metadata.stderrHandler)
}

@SerialVersionUID(1L)
case class ScalaTaskProp(jars: Array[String] = Array.empty,
                         mainClass: String = "",
                         args: Array[String] = Array.empty,
                         options: Array[String] = Array.empty,
                         env: Map[String, String] = Map.empty,
                         directory: String = "",
                         waitForTimeout: Long = -1) extends ProcessTaskProp("SCALA") {
  override def files: Seq[String] = jars.toSeq

  override def toTask(metadata: TaskMeta, fileMap: Map[String, String]): ProcessTaskConfig = ScalaTaskConfig(
    jars.map(s => fileMap(s)), mainClass, args, options, env, directory, waitForTimeout, metadata.stdoutHandler, metadata.stderrHandler)
}

@SerialVersionUID(1L)
case class PythonTaskProp(pyFile: String = "",
                          args: Array[String] = Array.empty,
                          env: Map[String, String] = Map.empty,
                          directory: String = "",
                          waitForTimeout: Long = -1) extends ProcessTaskProp("PYTHON") {
  override def files: Seq[String] = Seq(pyFile)

  override def toTask(metadata: TaskMeta, fileMap: Map[String, String]): ProcessTaskConfig = PythonTaskConfig(
    fileMap(pyFile), args, env, directory, waitForTimeout, metadata.stdoutHandler, metadata.stderrHandler)
}

@SerialVersionUID(1L)
case class UserAdd(user: String) extends ProcessTaskProp("USER_ADD") {
  override def toTask(metadata: TaskMeta, fileMap: Map[String, String]): ProcessTaskConfig = UserAddTaskConfig(user)
}