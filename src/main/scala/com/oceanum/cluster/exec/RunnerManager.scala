package com.oceanum.cluster.exec

import java.util.concurrent.atomic.AtomicInteger

import com.oceanum.client.Implicits.DurationHelper
import com.oceanum.common.Scheduler.scheduleOnce
import com.oceanum.common.{Environment, Log}

import scala.collection.concurrent.TrieMap

/**
 * @author chenmingkun
 * @date 2020/4/29
 */
object RunnerManager extends Log {
  type Prop = Operator[_ <: OperatorTask]
  private val num = Environment.EXEC_THREAD_NUM
  private val exec = RootRunner
  private val priorityMailbox: MailBox[Prop] = MailBox.priority(p => execute(p), num, (p1, p2) => p1.priority - p2.priority)
  private val outputManager: OutputManager = OutputManager.global
  private val tasks: TrieMap[Prop, Unit] = TrieMap()

  def submit(operatorProp: Prop): Hook = {
    operatorProp.eventListener.prepare()
    priorityMailbox.send(operatorProp)
    operatorProp.hook
  }

  def preparingTaskNum: Int = priorityMailbox.queueSize

  def runningTaskNum: Int = tasks.size

  def close(): Unit = {
    priorityMailbox.close()
    outputManager.close()
    log.info("execute manager closed")
  }

  private def execute(operatorProp: Prop): Unit = {
    operatorProp.eventListener.start()
    tasks + (operatorProp -> Unit)
    try {
      exec.run(operatorProp) match {
        case ExitCode.ERROR =>
          if (operatorProp.retryCount > 1) {
            val newOperatorProp = operatorProp.retry()
            val cancellable = scheduleOnce(fd"${newOperatorProp.retryInterval}") {
              this.submit(newOperatorProp)
            }
            newOperatorProp.receive(Hook(cancellable))
            operatorProp.eventListener.retry()
            log.info("task begin retry: " + operatorProp.name)
          } else {
            scheduleOnce(fd"10s") {
              operatorProp.prop.close()
            }
            operatorProp.eventListener.failed()
            log.info("task failed: " + operatorProp.name)
          }

        case ExitCode.OK =>
          scheduleOnce(fd"10s") {
            operatorProp.prop.close()
          }
          operatorProp.eventListener.success()
          log.info("task success: " + operatorProp.name)

        case ExitCode.KILL =>
          scheduleOnce(fd"10s") {
            operatorProp.prop.close()
          }
          operatorProp.eventListener.kill()
          log.info("task kill: " + operatorProp.name)

        case unSupport: ExitCode.UN_SUPPORT =>
          log.info(s"no executable executor exists for prop ${operatorProp.prop.getClass}")
          operatorProp.eventListener.failed(unSupport)
      }
    } catch {
      case e: Throwable =>
        val message = "this should never happen, or here is a bug"
        log.error(e, message)
        operatorProp.eventListener.failed(message)
    } finally {
      tasks - operatorProp
    }
  }
}
