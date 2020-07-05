package com.oceanum.client

import java.io.File

import com.oceanum.cluster.exec.OperatorTask
import com.oceanum.cluster.tasks._
import com.oceanum.common.Implicits.TaskMetadataHelper
import com.oceanum.file.FileClient

import scala.concurrent.{ExecutionContext, Future}
import com.oceanum.common.Implicits.PathHelper

@SerialVersionUID(1L)
trait TaskProp extends Serializable {

  def init(metadata: Metadata)(implicit executor: ExecutionContext): Future[OperatorTask]

  def taskType: String
}

@SerialVersionUID(1L)
abstract class ProcessTaskProp(task: String) extends TaskProp with Serializable {

  def files: Seq[String]

  def toTask(metadata: Metadata, fileMap: Map[String, String]): ProcessTask

  override def init(metadata: Metadata)(implicit executor: ExecutionContext): Future[ProcessTask] = {
    val fileMap: Map[String, String] = files.map(src => (src, metadata.execDir/new File(src).getName)).toMap
    fileMap.map(kv => FileClient.download(kv._1, kv._2))
      .reduce((f1, f2) => f1.flatMap(_ => f2))
      .map(_ => toTask(metadata, fileMap))
  }

  override def taskType: String = task
}

@SerialVersionUID(1L)
case class ShellTaskProp(cmd: Array[String] = Array.empty,
                         env: Map[String, String] = Map.empty,
                         directory: String = "",
                         waitForTimeout: Long = -1) extends ProcessTaskProp("SHELL") {
  override def files: Seq[String] = Seq.empty

  override def toTask(metadata: Metadata, fileMap: Map[String, String]): ProcessTask = ShellTask(
    cmd, env, directory, waitForTimeout, metadata.stdoutHandler, metadata.stderrHandler)
}

@SerialVersionUID(1L)
case class ShellScriptTaskProp(scriptFile: String = "",
                               args: Array[String] = Array.empty,
                               env: Map[String, String] = Map.empty,
                               directory: String = "",
                               waitForTimeout: Long = -1) extends ProcessTaskProp("SHELL_SCRIPT") {
  override def files: Seq[String] = Seq(scriptFile)

  override def toTask(metadata: Metadata, fileMap: Map[String, String]): ProcessTask = ShellScriptTask(
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

  override def toTask(metadata: Metadata, fileMap: Map[String, String]): ProcessTask = JavaTask(
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

  override def toTask(metadata: Metadata, fileMap: Map[String, String]): ProcessTask = ScalaTask(
    jars.map(s => fileMap(s)), mainClass, args, options, env, directory, waitForTimeout, metadata.stdoutHandler, metadata.stderrHandler)
}

@SerialVersionUID(1L)
case class PythonTaskProp(pyFile: String = "",
                          args: Array[String] = Array.empty,
                          env: Map[String, String] = Map.empty,
                          directory: String = "",
                          waitForTimeout: Long = -1) extends ProcessTaskProp("PYTHON") {
  override def files: Seq[String] = Seq(pyFile)

  override def toTask(metadata: Metadata, fileMap: Map[String, String]): ProcessTask = PythonTask(
    fileMap(pyFile), args, env, directory, waitForTimeout, metadata.stdoutHandler, metadata.stderrHandler)
}

@SerialVersionUID(1L)
case class SuUserTaskProp(user: String, prop: ProcessTaskProp) extends ProcessTaskProp("SU_USER_" + prop.taskType) {
  override def files: Seq[String] = prop.files

  override def toTask(metadata: Metadata, fileMap: Map[String, String]): ProcessTask = SuUserTask(user, prop.toTask(metadata, fileMap))
}