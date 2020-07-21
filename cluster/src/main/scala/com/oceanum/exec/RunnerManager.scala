package com.oceanum.exec

import java.util.Date
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.atomic.AtomicInteger

import com.oceanum.cluster.TaskInfoTrigger
import com.oceanum.common.Scheduler.scheduleOnce
import com.oceanum.common.{Environment, Log, NodeTaskInfo}
import com.oceanum.common.Implicits.EnvHelper

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

/**
 * @author chenmingkun
 * @date 2020/4/29
 */
object RunnerManager extends Log {
  type Prop = ExecutionTask[_ <: TaskConfig]
  private val num = Environment.EXEC_THREAD_NUM
  private val priorityMailbox = MailBox.priority[Prop](_.priority, num)(consume)
  private val runningNum: AtomicInteger = new AtomicInteger(0)
  private val successNum: AtomicInteger = new AtomicInteger(0)
  private val failedNum: AtomicInteger = new AtomicInteger(0)
  private val retryingNum: AtomicInteger = new AtomicInteger(0)
  private val killedNum: AtomicInteger = new AtomicInteger(0)
  private val completedNum: AtomicInteger = new AtomicInteger(0)
  private val runners = Environment.CLUSTER_NODE_RUNNERS_CLASSES
    .map(_.getConstructor().newInstance().asInstanceOf[TaskRunner])

  def getTaskInfo: NodeTaskInfo = {
    NodeTaskInfo(
      preparing = priorityMailbox.queueSize,
      running = runningNum.get(),
      success = successNum.get(),
      failed = failedNum.get(),
      retry = retryingNum.get(),
      killed = killedNum.get(),
      complete = completedNum.get()
    )
  }

  def submit(operatorProp: Prop): ExecutionHook = {
    operatorProp.eventListener.prepare(operatorProp.env.taskMeta)
    priorityMailbox.send(operatorProp)
    operatorProp.hook
  }

  def close(): Unit = {
    priorityMailbox.close
    runners.foreach(_.close())
    log.info("execute manager closed")
  }

  private def consume(operatorProp: Prop): Unit = {
    val prop = operatorProp.updateMeta(operatorProp.metadata.copy(startTime = new Date()))
    prop.eventListener.start(prop.metadata)
    incRunning()
    TaskInfoTrigger.trigger()
    try {
      run(prop) match {
        case ExitCode.ERROR(msg) =>
          if (prop.retryCount > 1) {
            val newOperatorProp = prop.retry()
            prop.eventListener.retry(newOperatorProp.metadata.copy(message = msg))
            log.info("task begin retry: " + newOperatorProp.name)
            incRetrying()
            val cancellable = scheduleOnce(newOperatorProp.retryInterval) {
              this.submit(newOperatorProp)
              decRetrying()
              TaskInfoTrigger.trigger()
            }
            newOperatorProp.receive(ExecutionHook(cancellable))
          } else {
            scheduleOnce(10.second) {
              prop.prop.close()
            }
            prop.eventListener.failed(prop.metadata.copy(message = msg, endTime = new Date()))
            log.info("task failed: " + prop.name)
            incFailed()
          }

        case ExitCode.OK =>
          scheduleOnce(10.second) {
            prop.prop.close()
          }
          prop.eventListener.success(prop.metadata.copy(endTime = new Date()))
          log.info("task success: " + prop.name)
          incSuccess()

        case ExitCode.KILL =>
          scheduleOnce(10.second) {
            prop.prop.close()
          }
          prop.eventListener.kill(prop.metadata.copy(endTime = new Date()))
          log.info("task kill: " + prop.name)
          incKilled()

        case unSupport: ExitCode.UN_SUPPORT =>
          log.error(s"no executable executor exists for prop ${prop.prop.getClass}")
          prop.eventListener.failed(prop.metadata.copy(message = s"task type not support: ${unSupport.taskType}", endTime = new Date()))
          prop.prop.close()
          incFailed()
      }
    } catch {
      case e: Throwable =>
        val message = "this should never happen, or here is a bug"
        log.error(e, message)
        prop.eventListener.failed(prop.metadata.copy(message = message, error = e))
        prop.prop.close()
        incFailed()
    } finally {
      decRunning()
      TaskInfoTrigger.trigger()
    }
  }

  private def run(prop: Prop): ExitCode = {
    val queue = new ArrayBlockingQueue[Try[Prop]](1)
    prop.prepareStart(ExecutionContext.global)
      .onComplete(queue.put)(Environment.CLUSTER_NODE_TASK_INIT_EXECUTOR)
    queue.take() match {
      case Success(task) =>
        runners.find(_.executable(task)) match {
        case Some(executor) =>
          if (task.hook.isKilled) {
            ExitCode.KILL
          } else {
            executor.run(task)
          }
        case None =>
          ExitCode.UN_SUPPORT(task.metadata.taskType)
      }
      case Failure(exception) =>
        exception.printStackTrace()
        ExitCode.ERROR(exception.getMessage)
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
