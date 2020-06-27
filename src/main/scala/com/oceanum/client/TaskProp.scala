package com.oceanum.client

import com.oceanum.cluster.exec.OperatorTask
import com.oceanum.cluster.tasks._

import scala.concurrent.duration.Duration
import Implicits.MetadataHelper
import com.oceanum.common.Environment


trait TaskProp {
  def toTask(metadata: Map[String, String]): OperatorTask

  def taskType: String
}

abstract class ProcessTaskProp extends TaskProp {
  override def toTask(metadata: Map[String, String]): ProcessTask
}

@SerialVersionUID(22222201L)
case class ShellTaskProp(cmd: Array[String] = Array.empty,
                         env: Map[String, String] = Map.empty,
                         directory: String = Environment.EXEC_WORK_DIR,
                         waitForTimeout: Long = -1) extends ProcessTaskProp {
  override def toTask(metadata: Map[String, String]): ProcessTask = ShellTask(
    cmd, env, directory, waitForTimeout, metadata.stdoutHandler, metadata.stderrHandler)

  override def taskType: String = "SHELL"
}

@SerialVersionUID(22222202L)
case class ShellScriptTaskProp(scriptFile: String,
                               args: Array[String] = Array.empty,
                               env: Map[String, String] = Map.empty,
                               directory: String = Environment.EXEC_WORK_DIR,
                               waitForTimeout: Long = -1) extends ProcessTaskProp {
  override def toTask(metadata: Map[String, String]): ProcessTask = ShellScriptTask(
    scriptFile, args, env, directory, waitForTimeout, metadata.stdoutHandler, metadata.stderrHandler)

  override def taskType: String = "SHELL_SCRIPT"
}

@SerialVersionUID(22222203L)
case class JavaTaskProp(jars: Array[String],
                        mainClass: String,
                        args: Array[String] = Array.empty,
                        options: Array[String] = Array.empty,
                        env: Map[String, String] = Map.empty,
                        directory: String = Environment.EXEC_WORK_DIR,
                        waitForTimeout: Long = -1) extends ProcessTaskProp {
  override def toTask(metadata: Map[String, String]): ProcessTask = JavaTask(
    jars, mainClass, args, options, env, directory, waitForTimeout, metadata.stdoutHandler, metadata.stderrHandler)

  override def taskType: String = "JAVA"
}

@SerialVersionUID(22222204L)
case class ScalaTaskProp(jars: Array[String],
                         mainClass: String,
                         args: Array[String] = Array.empty,
                         options: Array[String] = Array.empty,
                         env: Map[String, String] = Map.empty,
                         directory: String = Environment.EXEC_WORK_DIR,
                         waitForTimeout: Long = -1) extends ProcessTaskProp {
  override def toTask(metadata: Map[String, String]): ProcessTask = ScalaTask(
    jars, mainClass, args, options, env, directory, waitForTimeout, metadata.stdoutHandler, metadata.stderrHandler)

  override def taskType: String = "SCALA"
}

@SerialVersionUID(22222205L)
case class PythonTaskProp(pyFile: String,
                          args: Array[String] = Array.empty,
                          env: Map[String, String] = Map.empty,
                          directory: String = Environment.EXEC_WORK_DIR,
                          waitForTimeout: Long = -1) extends ProcessTaskProp {
  override def toTask(metadata: Map[String, String]): ProcessTask = PythonTask(
    pyFile, args, env, directory, waitForTimeout, metadata.stdoutHandler, metadata.stderrHandler)

  override def taskType: String = "PYTHON"
}

@SerialVersionUID(22222206L)
case class SuUserTaskProp(user: String, prop: ProcessTaskProp) extends TaskProp {
  override def toTask(metadata: Map[String, String]): ProcessTask = SuUserTask(user, prop.toTask(metadata))

  override def taskType: String = "SU_USER" + prop
}

object TaskPropBuilder {
  def python: PythonTaskPropBuilder = new PythonTaskPropBuilder(PythonTaskProp(pyFile = ""))
}

class PythonTaskPropBuilder(pythonTaskProp: PythonTaskProp) {

  def pyFile(path: String): PythonTaskPropBuilder = new PythonTaskPropBuilder(pythonTaskProp.copy(pyFile = path))

  def args(args: String*): PythonTaskPropBuilder = new PythonTaskPropBuilder(pythonTaskProp.copy(args = args.toArray))

  def directory(dir: String): PythonTaskPropBuilder = new PythonTaskPropBuilder(pythonTaskProp.copy(directory = dir))

  def waitForTimeout(timeout: String): PythonTaskPropBuilder = new PythonTaskPropBuilder(pythonTaskProp.copy(waitForTimeout = Duration(timeout).toMillis))

  def build: PythonTaskProp = pythonTaskProp
}