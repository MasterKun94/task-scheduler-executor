package com.oceanum.cluster.exec

import java.util.concurrent.atomic.AtomicInteger

import com.oceanum.client.Metadata
import com.oceanum.cluster.TaskInfoTrigger
import com.oceanum.common.Implicits.DurationHelper
import com.oceanum.common.Scheduler.scheduleOnce
import com.oceanum.common.{Environment, Log, NodeTaskInfoResponse}

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
  private val runningNum: AtomicInteger = new AtomicInteger(0)
  private val successNum: AtomicInteger = new AtomicInteger(0)
  private val failedNum: AtomicInteger = new AtomicInteger(0)
  private val retryingNum: AtomicInteger = new AtomicInteger(0)
  private val killedNum: AtomicInteger = new AtomicInteger(0)
  private val completedNum: AtomicInteger = new AtomicInteger(0)

  def getTaskInfo: NodeTaskInfoResponse = {
    NodeTaskInfoResponse(
      preparing = priorityMailbox.queueSize,
      running = runningNum.get(),
      success = successNum.get(),
      failed = failedNum.get(),
      retry = retryingNum.get(),
      killed = killedNum.get(),
      complete = completedNum.get()
    )
  }

  def submit(operatorProp: Prop): Hook = {
    operatorProp.eventListener.prepare()
    priorityMailbox.send(operatorProp)
    operatorProp.hook
  }

  def close(): Unit = {
    priorityMailbox.close()
    outputManager.close()
    log.info("execute manager closed")
  }

  private def execute(operatorProp: Prop): Unit = {
    operatorProp.eventListener.start()
    incRunning()
    TaskInfoTrigger.trigger()
    try {
      exec.run(operatorProp) match {
        case ExitCode.ERROR(msg) =>
          if (operatorProp.retryCount > 1) {
            val newOperatorProp = operatorProp.retry()
            operatorProp.eventListener.retry(Metadata("message" -> msg))
            log.info("task begin retry: " + operatorProp.name)
            incRetrying()
            val cancellable = scheduleOnce(fd"${newOperatorProp.retryInterval}") {
              this.submit(newOperatorProp)
              decRetrying()
              TaskInfoTrigger.trigger()
            }
            newOperatorProp.receive(Hook(cancellable))
          } else {
            scheduleOnce(fd"10s") {
              operatorProp.prop.close()
            }
            operatorProp.eventListener.failed(Metadata("message" -> msg))
            log.info("task failed: " + operatorProp.name)
            incFailed()
          }

        case ExitCode.OK =>
          scheduleOnce(fd"10s") {
            operatorProp.prop.close()
          }
          operatorProp.eventListener.success()
          log.info("task success: " + operatorProp.name)
          incSuccess()

        case ExitCode.KILL =>
          scheduleOnce(fd"10s") {
            operatorProp.prop.close()
          }
          operatorProp.eventListener.kill()
          log.info("task kill: " + operatorProp.name)
          incKilled()

        case unSupport: ExitCode.UN_SUPPORT =>
          log.error(s"no executable executor exists for prop ${operatorProp.prop.getClass}")
          operatorProp.eventListener.failed(Metadata("message" -> s"task type un support: ${unSupport.operatorClass}"))
          operatorProp.prop.close()
          incFailed()
      }
    } catch {
      case e: Throwable =>
        val message = "this should never happen, or here is a bug"
        log.error(e, message)
        operatorProp.eventListener.failed(Metadata("message" -> message))
        operatorProp.prop.close()
        incFailed()
    } finally {
      decRunning()
      TaskInfoTrigger.trigger()
    }
  }

  private def incRunning(): Unit = runningNum.incrementAndGet()
  private def decRunning(): Unit = runningNum.decrementAndGet()
  private def incRetrying(): Unit = retryingNum.incrementAndGet()
  private def decRetrying(): Unit = retryingNum.decrementAndGet()
  private def incFailed(): Unit = {
    failedNum.incrementAndGet()
    completedNum.incrementAndGet()
  }
  private def incKilled(): Unit = {
    killedNum.incrementAndGet()
    completedNum.incrementAndGet()
  }
  private def incSuccess(): Unit = {
    successNum.incrementAndGet()
    completedNum.incrementAndGet()
  }
}
