package com.oceanum.cluster.exec

import java.util.concurrent.{Delayed, TimeUnit}

import com.oceanum.common.{Environment, Log}

import scala.concurrent.duration.Duration

/**
 * @author chenmingkun
 * @date 2020/4/29
 */
object RunnerManager extends Log {
  type Prop = Operator[_ <: OperatorTask]
  private val num = Environment.EXEC_THREAD_NUM
  private val exec = RootRunner
  private val priorityMailbox: MailBox[Prop] = MailBox.priority(p => execute(p), num, (p1, p2) => p1.priority - p2.priority)
  private val delayedMailbox: MailBox[Delayed] = MailBox.delay(p => processDelay(p), 1)
  private val outputManager: OutputManager = OutputManager.global

  def submit(operatorProp: Prop): Hook = {
    operatorProp.eventListener.prepare()
    priorityMailbox.send(operatorProp)
    operatorProp.hook
  }

  def queueSize: Int = priorityMailbox.queueSize

  def close(): Unit = {
    delayedMailbox.close()
    priorityMailbox.close()
    outputManager.close()
    log.info("execute manager closed")
  }

  private def processDelay(delayed: Delayed): Unit = {
    delayed match {
      case closedProp: ClosedProp => closedProp.prop.close()

      case retryProp: RetryProp =>
        log.debug("resubmit retry task: " + retryProp.prop.name)
        submit(retryProp.prop)
    }
  }

  private def execute(operatorProp: Prop): Unit = {
    operatorProp.eventListener.start()
    val value: ExitCode = exec.run(operatorProp)
    value match {
      case ExitCode.ERROR =>
        if (operatorProp.retryCount > 1) {
          val newOperatorProp = operatorProp.retry()
          delayedMailbox.send(RetryProp(newOperatorProp, Duration(newOperatorProp.retryInterval).toMillis))
          operatorProp.eventListener.retry()
          log.info("task begin retry: " + operatorProp.name)
        } else {
          delayedMailbox.send(ClosedProp(operatorProp.prop))
          operatorProp.eventListener.failed()
          log.info("task failed: " + operatorProp.name)
        }

      case ExitCode.OK =>
        delayedMailbox.send(ClosedProp(operatorProp.prop))
        operatorProp.eventListener.success()
        log.info("task success: " + operatorProp.name)

      case ExitCode.KILL =>
        delayedMailbox.send(ClosedProp(operatorProp.prop))
        operatorProp.eventListener.kill()
        log.info("task kill: " + operatorProp.name)

      case unSupport: ExitCode.UN_SUPPORT =>
        log.info(s"no executable executor exists for prop ${operatorProp.prop.getClass}")
        operatorProp.eventListener.failed(unSupport)
    }
  }

  case class ClosedProp(prop: OperatorTask) extends Delayed {
    val time: Long = System.currentTimeMillis()

    override def getDelay(unit: TimeUnit): Long = unit.convert(time + 10000 - System.currentTimeMillis(), TimeUnit.MILLISECONDS);

    override def compareTo(o: Delayed): Int = (this.getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS)).toInt
  }

  case class RetryProp(prop: Prop, delay: Long) extends Delayed {
    val time: Long = System.currentTimeMillis()

    override def getDelay(unit: TimeUnit): Long = unit.convert(time + delay - System.currentTimeMillis(), TimeUnit.MILLISECONDS);

    override def compareTo(o: Delayed): Int = (this.getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS)).toInt
  }
}
