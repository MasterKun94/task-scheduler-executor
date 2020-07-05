package com.oceanum.client

import com.oceanum.cluster.exec.OperatorTask
import com.oceanum.cluster.tasks._
import com.oceanum.common.Environment
import com.oceanum.common.Implicits.MetadataHelper

@SerialVersionUID(1L)
trait TaskProp extends Serializable {
  def toTask(metadata: Metadata): OperatorTask

  def taskType: String
}

@SerialVersionUID(1L)
abstract class ProcessTaskProp(task: String) extends TaskProp with Serializable {
  override def toTask(metadata: Metadata): ProcessTask

  override def taskType: String = task
}

@SerialVersionUID(1L)
case class ShellTaskProp(cmd: Array[String] = Array.empty,
                         env: Map[String, String] = Map.empty,
                         directory: String = Environment.EXEC_WORK_DIR,
                         waitForTimeout: Long = -1) extends ProcessTaskProp("SHELL") {
  override def toTask(metadata: Metadata): ProcessTask = ShellTask(
    cmd, env, directory, waitForTimeout, metadata.stdoutHandler, metadata.stderrHandler)
}

@SerialVersionUID(1L)
case class ShellScriptTaskProp(scriptFile: String = "",
                               args: Array[String] = Array.empty,
                               env: Map[String, String] = Map.empty,
                               directory: String = Environment.EXEC_WORK_DIR,
                               waitForTimeout: Long = -1) extends ProcessTaskProp("SHELL_SCRIPT") {
  override def toTask(metadata: Metadata): ProcessTask = ShellScriptTask(
    scriptFile, args, env, directory, waitForTimeout, metadata.stdoutHandler, metadata.stderrHandler)
}

@SerialVersionUID(1L)
case class JavaTaskProp(jars: Array[String] = Array.empty,
                        mainClass: String = "",
                        args: Array[String] = Array.empty,
                        options: Array[String] = Array.empty,
                        env: Map[String, String] = Map.empty,
                        directory: String = Environment.EXEC_WORK_DIR,
                        waitForTimeout: Long = -1) extends ProcessTaskProp("JAVA") {
  override def toTask(metadata: Metadata): ProcessTask = JavaTask(
    jars, mainClass, args, options, env, directory, waitForTimeout, metadata.stdoutHandler, metadata.stderrHandler)
}

@SerialVersionUID(1L)
case class ScalaTaskProp(jars: Array[String] = Array.empty,
                         mainClass: String = "",
                         args: Array[String] = Array.empty,
                         options: Array[String] = Array.empty,
                         env: Map[String, String] = Map.empty,
                         directory: String = Environment.EXEC_WORK_DIR,
                         waitForTimeout: Long = -1) extends ProcessTaskProp("SCALA") {
  override def toTask(metadata: Metadata): ProcessTask = ScalaTask(
    jars, mainClass, args, options, env, directory, waitForTimeout, metadata.stdoutHandler, metadata.stderrHandler)
}

@SerialVersionUID(1L)
case class PythonTaskProp(pyFile: String = "",
                          args: Array[String] = Array.empty,
                          env: Map[String, String] = Map.empty,
                          directory: String = Environment.EXEC_WORK_DIR,
                          waitForTimeout: Long = -1) extends ProcessTaskProp("PYTHON") {
  override def toTask(metadata: Metadata): ProcessTask = PythonTask(
    pyFile, args, env, directory, waitForTimeout, metadata.stdoutHandler, metadata.stderrHandler)
}

@SerialVersionUID(1L)
case class SuUserTaskProp(user: String, prop: ProcessTaskProp) extends ProcessTaskProp("SU_USER_" + prop.taskType) {
  override def toTask(metadata: Metadata): ProcessTask = SuUserTask(user, prop.toTask(metadata))
}