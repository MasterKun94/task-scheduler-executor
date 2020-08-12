package com.oceanum.exec.runners

import java.io.{File, IOException, InputStream, OutputStream}
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

import com.oceanum.common.Environment
import com.oceanum.exec.ExitCode.ERROR
import com.oceanum.exec.{ExecutionHook, ExecutionTask, ExitCode, MailBox, StdHandler, TypedRunner}
import com.oceanum.exec.tasks.ProcessTaskConfig
import jnr.posix.POSIXFactory

import scala.collection.JavaConversions.{mapAsJavaMap, seqAsJavaList}
import scala.concurrent.duration.Duration

/**
 * @author chenmingkun
 * @date 2020/4/28
 */
object ProcessRunner extends TypedRunner[ProcessTaskConfig]("SHELL", "SHELL_SCRIPT", "JAVA", "SCALA", "PYTHON", "USER_ADD", "PLUGGABLE") {
  private val streamOutput = MailBox[(InputStream, StdHandler)](Environment.EXEC_THREAD_NUM * 2) { r =>
    r._2.handle(r._1)
  }

  override protected def typedRun(task: ExecutionTask[_ <: ProcessTaskConfig]): ExitCode = {
    val prop = task.prop
    val cmd = prop.propCmd.toList
    log.info("exec cmd: [ {} ]", cmd.mkString(" "))
    val builder: ProcessBuilder = new ProcessBuilder(cmd)
    builder.environment().putAll(prop.propEnv)
    val dir: File = {
      if (prop.propDirectory == null || prop.propDirectory.trim.equals("")) {
        new File(task.metadata.execDir)
      } else {
        val file = new File(prop.propDirectory)
        if (!file.exists()) {
          file.mkdirs()
        }
        file
      }
    }
    if (isKilled(task)) return ExitCode.KILL
    builder.directory(dir)
    val process = try {
      builder.start()
    } catch {
      case e: IOException =>
        e.printStackTrace()
        return ERROR(e)
    }
    val input = process.getInputStream
    val error = process.getErrorStream
    streamOutput.send(input -> prop.propStdoutHandler)
    streamOutput.send(error -> prop.propStderrHandler)
    sendRunEvent(task, new ShellExecutionHook(process))
    val value: ExitCode =
      try {
        val timeout = Duration(prop.propWaitForTimeout).toMillis
        val maxWait =
          if (timeout <= 0)
            Environment.EXEC_MAX_TIMEOUT.toMillis
          else
            timeout
        val exited = process.waitFor(maxWait, TimeUnit.MILLISECONDS)
        if (!exited) {
          kill(process)
        }
        ExitCode(process.exitValue())
      } catch {
        case _: InterruptedException =>
          if (process.isAlive) {
            process.destroyForcibly()
          }
          ExitCode(process.exitValue())
        case e: Exception =>
          e.printStackTrace()
          ERROR(e)
      }
    if (isKilled(task)) ExitCode.KILL else value
  }

  class ShellExecutionHook(process: Process) extends ExecutionHook {

    override def kill(): Boolean = {
      if (process.isAlive) {
        ProcessRunner.kill(process)
        true
      } else {
        false
      }
    }

    override def isKilled: Boolean = process.isAlive
  }

  override def close(): Unit = {
    streamOutput.close()
  }

  private def kill(process: Process): Unit = {
    try {
      val clazz = process.getClass
      val fPid = clazz.getDeclaredField("pid")
      if (!fPid.isAccessible) fPid.setAccessible(true)
      val pid = fPid.getInt(process).toString
      log.info("killing pid: {}", pid)
      val killing = new ProcessBuilder()
        .command("kill", "-15", pid)
        .start()
      val exited = process.waitFor(120, TimeUnit.SECONDS)

      if (!exited) {
        log.error("'kill -15' failed, start to destroy pid: {}", pid)
        process.destroy()
        if (process.isAlive) {
          process.destroyForcibly()
        }
      }
    } catch {
      case e: Throwable =>
        e.printStackTrace()
        process.destroyForcibly()
    }
  }
}
