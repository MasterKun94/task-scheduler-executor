package com.oceanum.exec.runners

import java.io.{File, IOException, InputStream}
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

import com.oceanum.exec.ExitCode.ERROR
import com.oceanum.exec._
import com.oceanum.exec.tasks._
import com.oceanum.common.Environment

import scala.collection.JavaConversions.{mapAsJavaMap, seqAsJavaList}

/**
 * @author chenmingkun
 * @date 2020/4/28
 */
class ProcessRunner extends TypedRunner[ProcessTaskConfig]("SHELL", "SHELL_SCRIPT", "JAVA", "SCALA", "PYTHON", "USER_ADD") {
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
        return ERROR(e.getMessage)
    }
    val input = process.getInputStream
    val error = process.getErrorStream
    streamOutput.send((input, prop.propStdoutHandler))
    streamOutput.send((error, prop.propStderrHandler))
    sendRunEvent(task, new ShellExecutionHook(process))
    val value: ExitCode =
      try {
        val maxWait =
          if (prop.propWaitForTimeout <= 0)
            Environment.EXEC_MAX_TIMEOUT.toMillis
          else
            prop.propWaitForTimeout
        val exited = process.waitFor(maxWait, TimeUnit.MILLISECONDS)
        if (exited) {
          ExitCode(process.exitValue())
        } else {
          ExitCode(process.destroyForcibly().exitValue())
        }
      } catch {
        case _: InterruptedException =>
          if (process.isAlive) {
            process.destroyForcibly()
          }
          ExitCode(process.exitValue())
        case e: Exception =>
          e.printStackTrace()
          ERROR(e.getMessage)
      }
    if (isKilled(task)) ExitCode.KILL else value
  }

  class ShellExecutionHook(process: Process) extends ExecutionHook {
    var ref = new AtomicBoolean(false)

    override def kill(): Boolean = {
      ref.set(false)
      !process.destroyForcibly().isAlive
    }

    override def isKilled: Boolean = ref.get()
  }

  override def close: Unit = {
    streamOutput.close
  }
}
