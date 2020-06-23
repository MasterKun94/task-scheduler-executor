package com.oceanum.api

import com.oceanum.exec.OperatorTask
import com.oceanum.exec.tasks._



trait TaskProp {
  def toTask: OperatorTask
}

abstract class ProcessTaskProp extends TaskProp {
  override def toTask: ProcessTask
}

@SerialVersionUID(22222201L)
case class ShellTaskProp(cmd: Array[String] = Array.empty,
                         env: Map[String, String] = Map.empty,
                         directory: Option[String] = None,
                         waitForTimeout: Long = -1,
                         stdoutHandler: () => InputStreamHandler,
                         stderrHandler: () => InputStreamHandler) extends ProcessTaskProp {
  override def toTask: ProcessTask = ShellTask(
    cmd, env, directory.getOrElse(""), waitForTimeout, stdoutHandler(), stderrHandler())
}

@SerialVersionUID(22222202L)
case class ShellScriptTaskProp(scriptFile: String,
                               args: Array[String] = Array.empty,
                               env: Map[String, String] = Map.empty,
                               directory: Option[String] = None,
                               waitForTimeout: Long = -1,
                               stdoutHandler: () => InputStreamHandler,
                               stderrHandler: () => InputStreamHandler) extends ProcessTaskProp {
  override def toTask: ProcessTask = ShellScriptTask(
    scriptFile, args, env, directory.getOrElse(""), waitForTimeout, stdoutHandler(), stderrHandler())
}

@SerialVersionUID(22222203L)
case class JavaTaskProp(jars: Array[String],
                        mainClass: String,
                        args: Array[String] = Array.empty,
                        options: Array[String] = Array.empty,
                        env: Map[String, String] = Map.empty,
                        directory: Option[String] = None,
                        waitForTimeout: Long = -1,
                        stdoutHandler: () => InputStreamHandler,
                        stderrHandler: () => InputStreamHandler) extends ProcessTaskProp {
  override def toTask: ProcessTask = JavaTask(
    jars, mainClass, args, options, env, directory.getOrElse(""), waitForTimeout, stdoutHandler(), stderrHandler())
}

@SerialVersionUID(22222204L)
case class ScalaTaskProp(jars: Array[String],
                         mainClass: String,
                         args: Array[String] = Array.empty,
                         options: Array[String] = Array.empty,
                         env: Map[String, String] = Map.empty,
                         directory: Option[String] = None,
                         waitForTimeout: Long = -1,
                         stdoutHandler: () => InputStreamHandler,
                         stderrHandler: () => InputStreamHandler) extends ProcessTaskProp {
  override def toTask: ProcessTask = ScalaTask(
    jars, mainClass, args, options, env, directory.getOrElse(""), waitForTimeout, stdoutHandler(), stderrHandler())
}

@SerialVersionUID(22222205L)
case class PythonTaskProp(pyFile: String,
                          args: Array[String] = Array.empty,
                          env: Map[String, String] = Map.empty,
                          directory: Option[String] = None,
                          waitForTimeout: Long = -1,
                          stdoutHandler: () => InputStreamHandler,
                          stderrHandler: () => InputStreamHandler) extends ProcessTaskProp {
  override def toTask: ProcessTask = PythonTask(
    pyFile, args, env, directory.getOrElse(""), waitForTimeout, stdoutHandler(), stderrHandler())
}

@SerialVersionUID(22222206L)
case class SuUserTaskProp(user: String, prop: ProcessTaskProp) extends TaskProp {
  override def toTask: ProcessTask = SuUserTask(user, prop.toTask)
}