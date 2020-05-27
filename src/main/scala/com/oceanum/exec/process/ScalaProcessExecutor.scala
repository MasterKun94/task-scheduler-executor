package com.oceanum.exec.process

import java.util.concurrent.atomic.AtomicBoolean

import com.oceanum.common.Environment
import com.oceanum.common.Environment.WINDOWS
import com.oceanum.exec.{Executor, ExecutorHook, ExitCode, Operator, OperatorProp, OutputManager}

import scala.sys.process._

/**
 * @author chenmingkun
 * @date 2020/5/10
 */
class ScalaProcessExecutor extends Executor[ProcessProp]  {
  override protected def typedExecute(operatorProp: Operator[_ <: ProcessProp]): ExitCode = {
    val prop = operatorProp.prop


    val export =
      if (Environment.OS == WINDOWS) "set"
      else "export"

    val dir =
      if (prop.propDirectory == null || prop.propDirectory.trim.equals("")) Environment.EXEC_WORK_DIR
      else prop.propDirectory
    val setEnv = if (prop.propEnv.isEmpty)
      ""
    else
      "&& " + prop.propEnv.toList.map(kv => s"${`export`} ${kv._1}=${kv._2}").mkString(" && ")
    val cmd = " && " + prop.propCmd.mkString(" ")

    val command: ProcessBuilder =
      if (Environment.OS == WINDOWS) {
        "cmd /c " + s"cmd /c cd $dir $setEnv $cmd"
      } else {
        s"cd $dir $setEnv $cmd"
      }
    LOGGER.info(s"exec cmd: [ $command ], prop: [ $prop ]")

    val process = command.run(ProcessLogger(prop.propStdoutHandler.handle, prop.propStderrHandler.handle))
    val hook: ExecutorHook = new ExecutorHook {
      val ref = new AtomicBoolean(false)

      override def kill(): Boolean = {
        process.destroy()
        ref.set(false)
        true
      }

      override def isKilled: Boolean = ref.get()
    }
    operatorProp.receive(hook)
    operatorProp.eventListener.running()
    val value = process.exitValue()
    if (hook.isKilled)
      ExitCode.KILL
    else
      ExitCode(value)
  }

  override def executable(p: OperatorProp): Boolean = {
    check(p)
  }

  @scala.annotation.tailrec
  private def check(operatorProp: OperatorProp): Boolean = {
    operatorProp match {
      case _: ShellProp => Environment.EXEC_SHELL_ENABLED
      case _: JavaProp => Environment.EXEC_JAVA_ENABLED
      case _: ScalaProp => Environment.EXEC_SCALA_ENABLED
      case _: PythonProp => Environment.EXEC_PYTHON_ENABLED
      case SuUserProp(_, prop) => check(prop)
    }
  }
}
