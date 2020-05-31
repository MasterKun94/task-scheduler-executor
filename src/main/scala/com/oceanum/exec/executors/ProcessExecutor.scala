package com.oceanum.exec.executors

import java.io.{File, IOException}
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

import com.oceanum.common.Environment
import com.oceanum.exec.tasks.{JavaTask, ProcessTask, PythonTask, ScalaTask, ShellTask, SuUserTask}
import com.oceanum.exec.{OperatorTask, _}

import scala.collection.JavaConversions.{mapAsJavaMap, seqAsJavaList}

/**
 * @author chenmingkun
 * @date 2020/4/28
 */
class ProcessExecutor(outputManager: OutputManager) extends TypedExecutor[ProcessTask] {

  override protected def typedExecute(operatorProp: Operator[_ <: ProcessTask]): ExitCode = {

    val prop = operatorProp.prop
    val cmd = prop.propCmd.toList
    LOGGER.info("exec cmd: [ {} ]", cmd.mkString(" "))
    val builder: ProcessBuilder = new ProcessBuilder(cmd)
    builder.environment().putAll(prop.propEnv)
    val dir: File = {
      if (prop.propDirectory == null || prop.propDirectory.trim.equals(""))
        new File("dummy").getAbsoluteFile.getParentFile
      else
        new File(prop.propDirectory)
    }
    builder.directory(dir)
    val process = try {
      builder.start()
    } catch {
      case _: IOException => return ExitCode.ERROR
    }
    val input = process.getInputStream
    val error = process.getErrorStream
    outputManager.submit(input, prop.propStdoutHandler)
    outputManager.submit(error, prop.propStderrHandler)
    val hook = new ShellExecutorHook(process)
    operatorProp.receive(hook)
    operatorProp.eventListener.running()
    val value =
      try {
        if (prop.propWaitForTimeout <= 0) {
          process.waitFor()
        } else {
          val exited = process.waitFor(prop.propWaitForTimeout, TimeUnit.MILLISECONDS)
          if (exited) {
            process.exitValue()
          } else {
            process.destroyForcibly().exitValue()
          }
        }
      } catch {
        case _: InterruptedException =>
          if (process.isAlive) {
            process.destroyForcibly()
          }
          process.exitValue()
        case e: Exception => e.printStackTrace()
          throw e
      }
    if (hook.isKilled) ExitCode.KILL else ExitCode(value)
  }

  class ShellExecutorHook(process: Process) extends ExecutorHook {
    var ref = new AtomicBoolean(false)

    override def kill(): Boolean = {
      ref.set(false)
      !process.destroyForcibly().isAlive
    }

    override def isKilled: Boolean = ref.get()
  }

  override def executable(p: OperatorTask): Boolean = {
    check(p)
  }

  @scala.annotation.tailrec
  private def check(operatorProp: OperatorTask): Boolean = {
    operatorProp match {
      case _: ShellTask => Environment.EXEC_SHELL_ENABLED
      case _: ScalaTask => Environment.EXEC_SCALA_ENABLED
      case _: JavaTask => Environment.EXEC_JAVA_ENABLED
      case _: PythonTask => Environment.EXEC_PYTHON_ENABLED
      case SuUserTask(_, prop) => check(prop)
    }
  }
}
