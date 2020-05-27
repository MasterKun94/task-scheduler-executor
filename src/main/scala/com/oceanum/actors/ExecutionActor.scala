package com.oceanum.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable}
import com.oceanum.client._
import com.oceanum.common.Environment
import com.oceanum.exec.EventListener._
import com.oceanum.exec.{EventListener, ExecuteManager, ExecutorHook}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
/**
 * @author chenmingkun
 * @date 2020/5/2
 */
class ExecutionActor(implicit executionContext: ExecutionContext) extends Actor with ActorLogging {

  private val listener: EventListener = new EventListener {
    override def prepare(message: Any): Unit = {
      log.info("prepare")
      self ! PrepareMessage(message)
    }
    override def start(message: Any): Unit = {
      log.info("start")
      self ! StartMessage(message)
    }
    override def running(message: Any): Unit = {
      log.info("running")
      self ! RunningMessage(message)
    }
    override def failed(message: Any): Unit = {
      log.info("failed")
      self ! FailedMessage(message)
    }
    override def success(message: Any): Unit = {
      log.info("success")
      self ! SuccessMessage(message)
    }
    override def retry(message: Any): Unit = {
      log.info("retry")
      self ! RetryMessage(message)
    }
    override def timeout(message: Any): Unit = {
      log.info("timeout")
      self ! TimeoutMessage(message)
    }
    override def kill(message: Any): Unit = {
      log.info("kill")
      self ! KillMessage(message)
    }
  }

  var execTimeoutMax: Cancellable = _

  override def preStart(): Unit = {
    execTimeoutMax = context.system.scheduler.scheduleOnce(FiniteDuration(Environment.EXEC_MAX_TIMEOUT._1, Environment.EXEC_MAX_TIMEOUT._2)) {
      context.stop(self)
    }
  }

  override def receive: Receive = {
    case ExecuteOperatorRequest(operatorMessage, scheduled) =>
      val operator = operatorMessage.toOperator(listener)
      log.info("receive operator: [{}], receive schedule check state request from [{}], start schedule with duration [{}]", operator, sender, scheduled.duration)
      implicit val hook: ExecutorHook = ExecuteManager.submit(operator)
      implicit val cancelable: Cancellable = context.system.scheduler.schedule(scheduled.duration, scheduled.duration) {
        self.tell(CheckStateOnce, sender())
      }
      implicit val actorHolder: ActorHolder = ActorHolder(sender())
      context.become(offline)
      sender ! ExecuteOperatorResponse(operatorMessage, scheduled)
    case message => println("unknown message: " + message)
  }

  private val offline_ : (ExecutorHook, Cancellable, ActorHolder) => Receive = offline(_, _, _)
  private val prepare_ : (ExecutorHook, Cancellable, ActorHolder) => Receive  = prepare(_, _, _)
  private val start_ : (ExecutorHook, Cancellable, ActorHolder) => Receive  = start(_, _, _)
  private val running_ : (ExecutorHook, Cancellable, ActorHolder) => Receive  = running(_, _, _)
  private val retry_ : (ExecutorHook, Cancellable, ActorHolder) => Receive  = retry(_, _, _)
  private val timeout_ : (ExecutorHook, Cancellable, ActorHolder) => Receive  = timeout(_, _, _)
  private val success_ : (ExecutorHook, Cancellable, ActorHolder) => Receive  = success(_, _, _)
  private val failed_ : (ExecutorHook, Cancellable, ActorHolder) => Receive  = failed(_, _, _)
  private val kill_ : (ExecutorHook, Cancellable, ActorHolder) => Receive  = kill(_, _, _)
  case class ActorHolder(actorRef: ActorRef)

  private def offline(implicit hook: ExecutorHook, cancellable: Cancellable, receiver: ActorHolder): Receive = {
    caseCheckState(OFFLINE, offline_)
      .orElse(casePrepare)
  }

  private def prepare(implicit hook: ExecutorHook, cancellable: Cancellable, receiver: ActorHolder): Receive = {
    caseCheckState(PREPARE, prepare_)
      .orElse(caseKillAction)
      .orElse(caseStart)
  }

  private def start(implicit hook: ExecutorHook, cancellable: Cancellable, receiver: ActorHolder): Receive = {
    caseCheckState(START, start_)
      .orElse(caseKillAction)
      .orElse(caseRunning)
  }

  private def running(implicit hook: ExecutorHook, cancellable: Cancellable, receiver: ActorHolder): Receive = {
    caseCheckState(RUNNING, running_)
      .orElse(caseKillAction)
      .orElse(caseSuccess)
      .orElse(caseFailed)
      .orElse(caseRetry)
      .orElse(caseTimeout)
      .orElse(caseKill)
  }

  private def retry(implicit hook: ExecutorHook, cancellable: Cancellable, receiver: ActorHolder): Receive = {
    caseCheckState(RETRY, retry_)
      .orElse(caseKillAction)
      .orElse(casePrepare)
  }

  private def timeout(implicit hook: ExecutorHook, cancellable: Cancellable, receiver: ActorHolder): Receive = {
    caseCheckState(TIMEOUT, timeout_)
      .orElse(caseKillAction)
      .orElse(caseRetry)
      .orElse(caseFailed)
  }

  private def success(implicit hook: ExecutorHook, cancellable: Cancellable, receiver: ActorHolder): Receive = {
    caseCheckState(SUCCESS, success_)
      .orElse(caseTerminateAction)
  }

  private def failed(implicit hook: ExecutorHook, cancellable: Cancellable, receiver: ActorHolder): Receive = {
    caseCheckState(FAILED, failed_)
      .orElse(caseTerminateAction)
  }

  private def kill(implicit hook: ExecutorHook, cancellable: Cancellable, receiver: ActorHolder): Receive = {
    caseCheckState(KILL, kill_)
      .orElse(caseTerminateAction)
      .orElse(caseFailed)
  }

  private def caseCheckState(state: State, receive: (ExecutorHook, Cancellable, ActorHolder) => Receive)(implicit hook: ExecutorHook, cancellable: Cancellable, receiver: ActorHolder): Receive = {
    case CheckStateOnce =>
      log.info("send state: [{}] to sender: [{}]", state, sender)
      sender ! state
    case CheckStateScheduled(duration, _) =>
      cancellable.cancel
      log.info("receive schedule check state request from [{}], start schedule with duration [{}]", sender, duration)
      val cancelable: Cancellable = context.system.scheduler.schedule(duration, duration) {
        self.tell(CheckStateOnce, receiver.actorRef)
      }
      context.become(receive(hook, cancelable, ActorHolder(receiver.actorRef)))
  }

  private def caseKillAction(implicit hook: ExecutorHook, cancellable: Cancellable, receiver: ActorHolder): Receive = {
    case KillAction =>
      hook.kill()
      self ! KillMessage("")
  }

  private def caseTerminateAction(implicit hook: ExecutorHook, cancellable: Cancellable, receiver: ActorHolder): Receive = {
    case TerminateAction =>
      log.info("terminate this action")
      if (cancellable != null || !cancellable.isCancelled) {
        cancellable.cancel()
      }
      execTimeoutMax.cancel()
      if (!hook.isKilled) {
        hook.kill()
      }
      context.stop(self)
  }

  private def casePrepare(implicit hook: ExecutorHook, cancellable: Cancellable, receiver: ActorHolder): Receive = {
    case _: PrepareMessage =>
      log.info("receive status changing, status: PREPARE")
      context.become(prepare)
      checkState
  }
  private def caseStart(implicit hook: ExecutorHook, cancellable: Cancellable, receiver: ActorHolder): Receive = {
    case _: StartMessage =>
      log.info("receive status changing, status: START")
      context.become(start)
      checkState
  }
  private def caseRunning(implicit hook: ExecutorHook, cancellable: Cancellable, receiver: ActorHolder): Receive = {
    case _: RunningMessage =>
      log.info("receive status changing, status: RUNNING")
      context.become(running)
      checkState
  }
  private def caseSuccess(implicit hook: ExecutorHook, cancellable: Cancellable, receiver: ActorHolder): Receive = {
    case _: SuccessMessage =>
      log.info("receive status changing, status: SUCCESS")
      context.become(success)
      checkState
  }
  private def caseFailed(implicit hook: ExecutorHook, cancellable: Cancellable, receiver: ActorHolder): Receive = {
    case _: FailedMessage =>
      log.info("receive status changing, status: FAILED")
      context.become(failed)
      checkState
  }
  private def caseRetry(implicit hook: ExecutorHook, cancellable: Cancellable, receiver: ActorHolder): Receive = {
    case _: RetryMessage =>
      log.info("receive status changing, status: RETRY")
      context.become(retry)
      checkState
  }
  private def caseKill(implicit hook: ExecutorHook, cancellable: Cancellable, receiver: ActorHolder): Receive = {
    case _: KillMessage =>
      log.info("receive status changing, status: KILL")
      context.become(kill)
      checkState
  }
  private def caseTimeout(implicit hook: ExecutorHook, cancellable: Cancellable, receiver: ActorHolder): Receive = {
    case _: TimeoutMessage =>
      log.info("receive status changing, status: TIMEOUT")
      context.become(timeout)
      checkState
  }

  private def checkState(implicit receiver: ActorHolder): Unit = {
    self.tell(CheckStateOnce, receiver.actorRef)
  }
}