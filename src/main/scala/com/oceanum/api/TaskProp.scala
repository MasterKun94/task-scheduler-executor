package com.oceanum.api

import com.oceanum.exec.OperatorTask
import com.oceanum.exec.tasks.{JavaTask, ProcessTask, PythonTask, ScalaTask, ShellScriptTask, ShellTask, SuUserTask}



trait TaskProp {
  def toTask: OperatorTask
}

abstract class ProcessTaskProp extends TaskProp {
  override def toTask: ProcessTask
}

case class ShellTaskProp(cmd: Array[String] = Array.empty,
                         env: Map[String, String] = Map.empty,
                         directory: Option[String] = None,
                         waitForTimeout: Long = -1,
                         stdoutLineHandler: () => InputStreamHandler,
                         stderrLineHandler: () => InputStreamHandler) extends ProcessTaskProp {
  override def toTask: ProcessTask = ShellTask(
    cmd, env, directory.getOrElse(""), waitForTimeout, stdoutLineHandler(), stderrLineHandler())
}

case class ShellScriptTaskProp(scriptFile: String,
                               args: Array[String] = Array.empty,
                               env: Map[String, String] = Map.empty,
                               directory: Option[String] = None,
                               waitForTimeout: Long = -1,
                               stdoutLineHandler: () => InputStreamHandler,
                               stderrLineHandler: () => InputStreamHandler) extends ProcessTaskProp {
  override def toTask: ProcessTask = ShellScriptTask(
    scriptFile, args, env, directory.getOrElse(""), waitForTimeout, stdoutLineHandler(), stderrLineHandler())
}

case class JavaTaskProp(jars: Array[String],
                        mainClass: String,
                        args: Array[String] = Array.empty,
                        options: Array[String] = Array.empty,
                        env: Map[String, String] = Map.empty,
                        directory: Option[String] = None,
                        waitForTimeout: Long = -1,
                        stdoutLineHandler: () => InputStreamHandler,
                        stderrLineHandler: () => InputStreamHandler) extends ProcessTaskProp {
  override def toTask: ProcessTask = JavaTask(
    jars, mainClass, args, options, env, directory.getOrElse(""), waitForTimeout, stdoutLineHandler(), stderrLineHandler())
}

case class ScalaTaskProp(jars: Array[String],
                         mainClass: String,
                         args: Array[String] = Array.empty,
                         options: Array[String] = Array.empty,
                         env: Map[String, String] = Map.empty,
                         directory: Option[String] = None,
                         waitForTimeout: Long = -1,
                         stdoutLineHandler: () => InputStreamHandler,
                         stderrLineHandler: () => InputStreamHandler) extends ProcessTaskProp {
  override def toTask: ProcessTask = ScalaTask(
    jars, mainClass, args, options, env, directory.getOrElse(""), waitForTimeout, stdoutLineHandler(), stderrLineHandler())
}
case class PythonTaskProp(pyFile: String,
                          args: Array[String] = Array.empty,
                          options: Array[String] = Array.empty,
                          env: Map[String, String] = Map.empty,
                          directory: String = ".",
                          waitForTimeout: Long = -1,
                          stdoutLineHandler: () => InputStreamHandler,
                          stderrLineHandler: () => InputStreamHandler) extends ProcessTaskProp {
  override def toTask: ProcessTask = PythonTask(
    pyFile, args, options, env, directory, waitForTimeout, stdoutLineHandler(), stderrLineHandler())
}

case class SuUserTaskProp(user: String, prop: ProcessTaskProp) extends TaskProp {
  override def toTask: ProcessTask = SuUserTask(user, prop.toTask)
}