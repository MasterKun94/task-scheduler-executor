package com.oceanum.exec

import java.util.concurrent.{Delayed, TimeUnit}

import com.oceanum.common.{Environment, Log}

/**
 * @author chenmingkun
 * @date 2020/4/29
 */
object ExecuteManager extends Log {
  type Prop = Operator[_ <: OperatorTask]
  private val num = Environment.EXEC_THREAD_NUM
  private val exec = RootExecutor
  private val priorityMailbox: MailBox[Prop] = MailBox.priority(p => execute(p), num, (p1, p2) => p1.priority - p2.priority)
  private val delayedMailbox: MailBox[Delayed] = MailBox.delay(p => processDelay(p), 1)
  private val outputManager: OutputManager = OutputManager.global

  def submit(operatorProp: Prop): ExecutorHook = {
    operatorProp.eventListener.prepare()
    priorityMailbox.send(operatorProp)
    operatorProp.hook
  }

  def queueSize: Int = priorityMailbox.queueSize

  def close(): Unit = {
    delayedMailbox.close()
    priorityMailbox.close()
    outputManager.close()
    LOGGER.info("execute manager closed")
  }

  private def processDelay(delayed: Delayed): Unit = {
    delayed match {
      case closedProp: ClosedProp => closedProp.prop.close()

      case retryProp: RetryProp =>
        LOGGER.debug("resubmit retry task: " + retryProp.prop.name)
        submit(retryProp.prop)
    }
  }

  private def execute(operatorProp: Prop): Unit = {
    operatorProp.eventListener.start()
    val value: ExitCode = exec.execute(operatorProp)
    value match {
      case ExitCode.ERROR =>
        if (operatorProp.retryCount > 1) {
          val newOperatorProp = operatorProp.copy(retryCount = operatorProp.retryCount - 1)
          delayedMailbox.send(RetryProp(newOperatorProp, newOperatorProp.retryInterval))
          operatorProp.eventListener.retry()
          LOGGER.info("task begin retry: " + operatorProp.name)
        } else {
          delayedMailbox.send(ClosedProp(operatorProp.prop))
          operatorProp.eventListener.failed()
          LOGGER.info("task failed: " + operatorProp.name)
        }

      case ExitCode.OK =>
        delayedMailbox.send(ClosedProp(operatorProp.prop))
        operatorProp.eventListener.success()
        LOGGER.info("task success: " + operatorProp.name)

      case ExitCode.KILL =>
        delayedMailbox.send(ClosedProp(operatorProp.prop))
        operatorProp.eventListener.kill()
        LOGGER.info("task kill: " + operatorProp.name)

      case unSupport: ExitCode.UN_SUPPORT =>
        LOGGER.info(s"no executable executor exists for prop ${operatorProp.prop.getClass}")
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
