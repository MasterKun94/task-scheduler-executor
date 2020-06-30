package com.oceanum.cluster.exec

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.ActorRef
import akka.cluster.client.ClusterClient.Publish
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.singleton.{ClusterSingletonProxy, ClusterSingletonProxySettings}
import com.oceanum.client.Implicits.DurationHelper
import com.oceanum.cluster.TaskInfoGetter
import com.oceanum.common.Scheduler.scheduleOnce
import com.oceanum.common.{Environment, Log, NodeTaskInfoResponse, Scheduler}

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
  private val successNum: AtomicInteger = new AtomicInteger(0)
  private val failedNum: AtomicInteger = new AtomicInteger(0)
  private val killedNum: AtomicInteger = new AtomicInteger(0)
  private val completedNum: AtomicInteger = new AtomicInteger(0)

  def getTaskInfo: NodeTaskInfoResponse = {
    NodeTaskInfoResponse(
      preparing = priorityMailbox.queueSize,
      running = tasks.size,
      success = successNum.get(),
      failed = failedNum.get(),
      killed = killedNum.get(),
      complete = completedNum.get()
    )
  }

  private def updateTask(action: => Unit): Unit = {
    action
    TaskInfoGetter.trigger()
  }

  def submit(operatorProp: Prop): Hook = {
    operatorProp.eventListener.prepare()
    priorityMailbox.send(operatorProp)
    operatorProp.hook
  }

  def preparingTaskNum: Int = priorityMailbox.queueSize

  def runningTaskNum: Int = tasks.size

  def close(): Unit = {
    TaskInfoGetter.close()
    priorityMailbox.close()
    outputManager.close()
    log.info("execute manager closed")
  }

  private def execute(operatorProp: Prop): Unit = {
    operatorProp.eventListener.start()
    updateTask(tasks + (operatorProp -> Unit))
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
            updateTask(failedNum.incrementAndGet())
            log.info("task failed: " + operatorProp.name)
          }

        case ExitCode.OK =>
          scheduleOnce(fd"10s") {
            operatorProp.prop.close()
          }
          operatorProp.eventListener.success()
          updateTask(successNum.incrementAndGet())
          updateTask(completedNum.incrementAndGet())
          log.info("task success: " + operatorProp.name)

        case ExitCode.KILL =>
          scheduleOnce(fd"10s") {
            operatorProp.prop.close()
          }
          operatorProp.eventListener.kill()
          updateTask(killedNum.incrementAndGet())
          updateTask(completedNum.incrementAndGet())
          log.info("task kill: " + operatorProp.name)

        case unSupport: ExitCode.UN_SUPPORT =>
          log.info(s"no executable executor exists for prop ${operatorProp.prop.getClass}")
          updateTask(failedNum.incrementAndGet())
          updateTask(completedNum.incrementAndGet())
          operatorProp.eventListener.failed(unSupport)
      }
    } catch {
      case e: Throwable =>
        val message = "this should never happen, or here is a bug"
        log.error(e, message)
        operatorProp.eventListener.failed(message)
    } finally {
      updateTask(tasks - operatorProp)
    }
  }
}
