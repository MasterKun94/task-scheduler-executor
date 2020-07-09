package com.oceanum.cluster.runners

import java.io.{File, IOException, InputStream}
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

import com.oceanum.cluster.exec._
import com.oceanum.cluster.tasks._
import com.oceanum.common.Environment

import scala.collection.JavaConversions.{mapAsJavaMap, seqAsJavaList}

/**
 * @author chenmingkun
 * @date 2020/4/28
 */
class ProcessRunner extends TypedRunner[ProcessTask]("SHELL", "SHELL_SCRIPT", "JAVA", "SCALA", "PYTHON", "USER_ADD") {
  private val streamOutput = MailBox[(InputStream, InputStreamHandler)](Environment.EXEC_THREAD_NUM * 2) { r =>
    r._2.handle(r._1)
  }

  override protected def typedRun(operatorProp: Operator[_ <: ProcessTask]): ExitCode = {
    val prop = operatorProp.prop
    val cmd = prop.propCmd.toList
    log.info("exec cmd: [ {} ]", cmd.mkString(" "))
    val builder: ProcessBuilder = new ProcessBuilder(cmd)
    builder.environment().putAll(prop.propEnv)
    val dir: File = {
      if (prop.propDirectory == null || prop.propDirectory.trim.equals("")) {
        new File(operatorProp.metadata.execDir)
      } else {
        val file = new File(prop.propDirectory)
        if (!file.exists()) {
          file.mkdirs()
        }
        file
      }
    }
    if (operatorProp.hook.isKilled) return ExitCode.KILL
    builder.directory(dir)
    val process = try {
      builder.start()
    } catch {
      case e: IOException =>
        e.printStackTrace()
        return ExitCode.ERROR(e.getMessage)
    }
    val input = process.getInputStream
    val error = process.getErrorStream
    streamOutput.send((input, prop.propStdoutHandler))
    streamOutput.send((error, prop.propStderrHandler))
    val hook = new ShellHook(process)
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

  class ShellHook(process: Process) extends Hook {
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
