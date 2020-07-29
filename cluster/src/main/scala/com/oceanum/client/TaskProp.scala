package com.oceanum.client

import com.oceanum.common.RichTaskMeta
import com.oceanum.exec.TaskConfig
import com.oceanum.exec.tasks.SysTasks.UserAddTaskConfig
import com.oceanum.exec.tasks.{JavaTaskConfig, ProcessTaskConfig, PythonTaskConfig, ScalaTaskConfig, ShellScriptTaskConfig, ShellTaskConfig}
import com.oceanum.expr.ValidateParser

@SerialVersionUID(1L)
abstract class TaskProp(val taskType: String) extends Serializable {

  def toTask(metadata: RichTaskMeta): TaskConfig

  def validate(): Unit
}

@SerialVersionUID(1L)
abstract class ProcessTaskProp(taskType: String) extends TaskProp(taskType) with Serializable {

  override def toTask(metadata: RichTaskMeta): ProcessTaskConfig
}

@SerialVersionUID(1L)
case class ShellTaskProp(cmd: Array[String] = Array.empty,
                         env: Map[String, String] = Map.empty,
                         directory: String = "",
                         waitForTimeout: String = "24h") extends ProcessTaskProp("SHELL") {
  override def toTask(metadata: RichTaskMeta): ProcessTaskConfig = ShellTaskConfig(
    cmd, env, directory, waitForTimeout, metadata.stdoutHandler, metadata.stderrHandler)

  override def validate(): Unit = {
    cmd.foreach(ValidateParser.parse)
    env.foreach(t => ValidateParser.parse(t._2))
    ValidateParser.parse(directory)
  }
}

@SerialVersionUID(1L)
case class ShellScriptTaskProp(scriptFile: String = "",
                               args: Array[String] = Array.empty,
                               env: Map[String, String] = Map.empty,
                               directory: String = "",
                               waitForTimeout: String = "24h") extends ProcessTaskProp("SHELL_SCRIPT") {

  override def toTask(metadata: RichTaskMeta): ProcessTaskConfig = ShellScriptTaskConfig(
    scriptFile, args, env, directory, waitForTimeout, metadata.stdoutHandler, metadata.stderrHandler)

  override def validate(): Unit = {
    args.foreach(ValidateParser.parse)
    env.foreach(t => ValidateParser.parse(t._2))
    ValidateParser.parse(directory)
    ValidateParser.parse(scriptFile)
  }
}

@SerialVersionUID(1L)
case class JavaTaskProp(jars: Array[String] = Array.empty,
                        mainClass: String = "",
                        args: Array[String] = Array.empty,
                        options: Array[String] = Array.empty,
                        env: Map[String, String] = Map.empty,
                        directory: String = "",
                        waitForTimeout: String = "24h") extends ProcessTaskProp("JAVA") {

  override def toTask(metadata: RichTaskMeta): ProcessTaskConfig = JavaTaskConfig(
    jars, mainClass, args, options, env, directory, waitForTimeout, metadata.stdoutHandler, metadata.stderrHandler)

  override def validate(): Unit = {
    options.foreach(ValidateParser.parse)
    args.foreach(ValidateParser.parse)
    jars.foreach(ValidateParser.parse)
    env.foreach(t => ValidateParser.parse(t._2))
    ValidateParser.parse(directory)
    ValidateParser.parse(mainClass)
  }
}

@SerialVersionUID(1L)
case class ScalaTaskProp(jars: Array[String] = Array.empty,
                         mainClass: String = "",
                         args: Array[String] = Array.empty,
                         options: Array[String] = Array.empty,
                         env: Map[String, String] = Map.empty,
                         directory: String = "",
                         waitForTimeout: String = "24h") extends ProcessTaskProp("SCALA") {

  override def toTask(metadata: RichTaskMeta): ProcessTaskConfig = ScalaTaskConfig(
    jars, mainClass, args, options, env, directory, waitForTimeout, metadata.stdoutHandler, metadata.stderrHandler)

  override def validate(): Unit = {
    options.foreach(ValidateParser.parse)
    args.foreach(ValidateParser.parse)
    jars.foreach(ValidateParser.parse)
    env.foreach(t => ValidateParser.parse(t._2))
    ValidateParser.parse(directory)
    ValidateParser.parse(mainClass)
  }
}

@SerialVersionUID(1L)
case class PythonTaskProp(pyFile: String = "",
                          args: Array[String] = Array.empty,
                          env: Map[String, String] = Map.empty,
                          directory: String = "",
                          waitForTimeout: String = "24h") extends ProcessTaskProp("PYTHON") {

  override def toTask(metadata: RichTaskMeta): ProcessTaskConfig = PythonTaskConfig(
    pyFile, args, env, directory, waitForTimeout, metadata.stdoutHandler, metadata.stderrHandler)

  override def validate(): Unit = {
    args.foreach(ValidateParser.parse)
    env.foreach(t => ValidateParser.parse(t._2))
    ValidateParser.parse(directory)
    ValidateParser.parse(pyFile)
  }
}

@SerialVersionUID(1L)
case class UserAdd(user: String) extends ProcessTaskProp("USER_ADD") {
  override def toTask(metadata: RichTaskMeta): ProcessTaskConfig = UserAddTaskConfig(user)

  override def validate(): Unit = ValidateParser.parse(user)
}