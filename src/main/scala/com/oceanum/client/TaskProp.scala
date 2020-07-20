package com.oceanum.client

import com.oceanum.cluster.exec.TaskConfig
import com.oceanum.cluster.tasks.SysTasks.UserAddTaskConfig
import com.oceanum.cluster.tasks._
import com.oceanum.common.StringParser

@SerialVersionUID(1L)
abstract class TaskProp(val taskType: String) extends Serializable {

  def toTask(metadata: RichTaskMeta): TaskConfig

  def validate: Unit
}

@SerialVersionUID(1L)
abstract class ProcessTaskProp(taskType: String) extends TaskProp(taskType) with Serializable {

  override def toTask(metadata: RichTaskMeta): ProcessTaskConfig
}

@SerialVersionUID(1L)
case class ShellTaskProp(cmd: Array[String] = Array.empty,
                         env: Map[String, String] = Map.empty,
                         directory: String = "",
                         waitForTimeout: Long = -1) extends ProcessTaskProp("SHELL") {
  override def toTask(metadata: RichTaskMeta): ProcessTaskConfig = ShellTaskConfig(
    cmd, env, directory, waitForTimeout, metadata.stdoutHandler, metadata.stderrHandler)

  override def validate: Unit = {
    cmd.foreach(StringParser.validate)
    env.foreach(t => StringParser.validate(t._2))
    StringParser.validate(directory)
  }
}

@SerialVersionUID(1L)
case class ShellScriptTaskProp(scriptFile: String = "",
                               args: Array[String] = Array.empty,
                               env: Map[String, String] = Map.empty,
                               directory: String = "",
                               waitForTimeout: Long = -1) extends ProcessTaskProp("SHELL_SCRIPT") {

  override def toTask(metadata: RichTaskMeta): ProcessTaskConfig = ShellScriptTaskConfig(
    scriptFile, args, env, directory, waitForTimeout, metadata.stdoutHandler, metadata.stderrHandler)

  override def validate: Unit = {
    args.foreach(StringParser.validate)
    env.foreach(t => StringParser.validate(t._2))
    StringParser.validate(directory)
    StringParser.validate(scriptFile)
  }
}

@SerialVersionUID(1L)
case class JavaTaskProp(jars: Array[String] = Array.empty,
                        mainClass: String = "",
                        args: Array[String] = Array.empty,
                        options: Array[String] = Array.empty,
                        env: Map[String, String] = Map.empty,
                        directory: String = "",
                        waitForTimeout: Long = -1) extends ProcessTaskProp("JAVA") {

  override def toTask(metadata: RichTaskMeta): ProcessTaskConfig = JavaTaskConfig(
    jars, mainClass, args, options, env, directory, waitForTimeout, metadata.stdoutHandler, metadata.stderrHandler)

  override def validate: Unit = {
    options.foreach(StringParser.validate)
    args.foreach(StringParser.validate)
    jars.foreach(StringParser.validate)
    env.foreach(t => StringParser.validate(t._2))
    StringParser.validate(directory)
    StringParser.validate(mainClass)
  }
}

@SerialVersionUID(1L)
case class ScalaTaskProp(jars: Array[String] = Array.empty,
                         mainClass: String = "",
                         args: Array[String] = Array.empty,
                         options: Array[String] = Array.empty,
                         env: Map[String, String] = Map.empty,
                         directory: String = "",
                         waitForTimeout: Long = -1) extends ProcessTaskProp("SCALA") {

  override def toTask(metadata: RichTaskMeta): ProcessTaskConfig = ScalaTaskConfig(
    jars, mainClass, args, options, env, directory, waitForTimeout, metadata.stdoutHandler, metadata.stderrHandler)

  override def validate: Unit = {
    options.foreach(StringParser.validate)
    args.foreach(StringParser.validate)
    jars.foreach(StringParser.validate)
    env.foreach(t => StringParser.validate(t._2))
    StringParser.validate(directory)
    StringParser.validate(mainClass)
  }
}

@SerialVersionUID(1L)
case class PythonTaskProp(pyFile: String = "",
                          args: Array[String] = Array.empty,
                          env: Map[String, String] = Map.empty,
                          directory: String = "",
                          waitForTimeout: Long = -1) extends ProcessTaskProp("PYTHON") {

  override def toTask(metadata: RichTaskMeta): ProcessTaskConfig = PythonTaskConfig(
    pyFile, args, env, directory, waitForTimeout, metadata.stdoutHandler, metadata.stderrHandler)

  override def validate: Unit = {
    args.foreach(StringParser.validate)
    env.foreach(t => StringParser.validate(t._2))
    StringParser.validate(directory)
    StringParser.validate(pyFile)
  }
}

@SerialVersionUID(1L)
case class UserAdd(user: String) extends ProcessTaskProp("USER_ADD") {
  override def toTask(metadata: RichTaskMeta): ProcessTaskConfig = UserAddTaskConfig(user)

  override def validate: Unit = StringParser.validate(user)
}