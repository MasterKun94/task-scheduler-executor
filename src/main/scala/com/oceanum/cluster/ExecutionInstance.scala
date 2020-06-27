package com.oceanum.cluster

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable}
import com.oceanum.client.Implicits._
import com.oceanum.client.StateHandler
import com.oceanum.cluster.exec.State._
import com.oceanum.cluster.exec.{EventListener, ExecuteManager, ExecutorHook}
import com.oceanum.common._

import scala.concurrent.duration.FiniteDuration
/**
 * @author chenmingkun
 * @date 2020/5/2
 */
class ExecutionInstance extends Actor with ActorLogging {

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

  import context.dispatcher
  override def preStart(): Unit = {
    execTimeoutMax = context.system.scheduler.scheduleOnce(FiniteDuration(Environment.EXEC_MAX_TIMEOUT._1, Environment.EXEC_MAX_TIMEOUT._2)) {
      context.stop(self)
    }
  }

  override def receive: Receive = {
    case ExecuteOperatorRequest(operatorMessage, handler) =>
      val operator = operatorMessage.toOperator(listener)
      val finiteDuration = fd"${handler.checkInterval()}"
      log.info("receive operator: [{}], receive schedule check state request from [{}], start schedule with duration [{}]", operator, sender, finiteDuration)
      implicit val hook: ExecutorHook = ExecuteManager.submit(operator)
      implicit val cancelable: Cancellable = context
        .system.scheduler
        .schedule(finiteDuration, finiteDuration) {
          self.tell(CheckState, sender())
        }
      implicit val clientHolder: ClientHolder = ClientHolder(sender())
      context.become(offline)
      sender ! ExecuteOperatorResponse(operatorMessage, handler)
    case message => println("unknown message: " + message)
  }

  private val offline_ : (ExecutorHook, Cancellable, ClientHolder) => Receive = offline(_, _, _)
  private val prepare_ : (ExecutorHook, Cancellable, ClientHolder) => Receive  = prepare(_, _, _)
  private val start_ : (ExecutorHook, Cancellable, ClientHolder) => Receive  = start(_, _, _)
  private val running_ : (ExecutorHook, Cancellable, ClientHolder) => Receive  = running(_, _, _)
  private val retry_ : (ExecutorHook, Cancellable, ClientHolder) => Receive  = retry(_, _, _)
  private val timeout_ : (ExecutorHook, Cancellable, ClientHolder) => Receive  = timeout(_, _, _)
  private val success_ : (ExecutorHook, Cancellable, ClientHolder) => Receive  = success(_, _, _)
  private val failed_ : (ExecutorHook, Cancellable, ClientHolder) => Receive  = failed(_, _, _)
  private val kill_ : (ExecutorHook, Cancellable, ClientHolder) => Receive  = kill(_, _, _)
  case class ClientHolder(client: ActorRef)

  private def offline(implicit hook: ExecutorHook, cancellable: Cancellable, client: ClientHolder): Receive = {
    caseCheckState(OFFLINE, offline_)
      .orElse(caseKillAction)
      .orElse(casePrepare)
      .orElse(caseKill)
  }

  private def prepare(implicit hook: ExecutorHook, cancellable: Cancellable, client: ClientHolder): Receive = {
    caseCheckState(PREPARE, prepare_)
      .orElse(caseKillAction)
      .orElse(caseStart)
      .orElse(caseKill)
  }

  private def start(implicit hook: ExecutorHook, cancellable: Cancellable, client: ClientHolder): Receive = {
    caseCheckState(START, start_)
      .orElse(caseKillAction)
      .orElse(caseRunning)
      .orElse(caseKill)
  }

  private def running(implicit hook: ExecutorHook, cancellable: Cancellable, client: ClientHolder): Receive = {
    caseCheckState(RUNNING, running_)
      .orElse(caseKillAction)
      .orElse(caseSuccess)
      .orElse(caseFailed)
      .orElse(caseRetry)
      .orElse(caseTimeout)
      .orElse(caseKill)
  }

  private def retry(implicit hook: ExecutorHook, cancellable: Cancellable, client: ClientHolder): Receive = {
    caseCheckState(RETRY, retry_)
      .orElse(caseKillAction)
      .orElse(casePrepare)
      .orElse(caseKill)
  }

  private def timeout(implicit hook: ExecutorHook, cancellable: Cancellable, client: ClientHolder): Receive = {
    caseCheckState(TIMEOUT, timeout_)
      .orElse(caseKillAction)
      .orElse(caseRetry)
      .orElse(caseFailed)
      .orElse(caseKill)
  }

  private def success(implicit hook: ExecutorHook, cancellable: Cancellable, client: ClientHolder): Receive = {
    caseCheckState(SUCCESS, success_)
      .orElse(caseTerminateAction)
  }

  private def failed(implicit hook: ExecutorHook, cancellable: Cancellable, client: ClientHolder): Receive = {
    caseCheckState(FAILED, failed_)
      .orElse(caseTerminateAction)
  }

  private def kill(implicit hook: ExecutorHook, cancellable: Cancellable, client: ClientHolder): Receive = {
    caseCheckState(KILL, kill_)
      .orElse(caseTerminateAction)
      .orElse(caseFailed)
  }

  private def caseCheckState(state: State, receive: (ExecutorHook, Cancellable, ClientHolder) => Receive)(implicit hook: ExecutorHook, cancellable: Cancellable, client: ClientHolder): Receive = {
    case CheckState =>
      log.info("send state: [{}] to sender: [{}]", state, sender)
      sender ! state
    case handler: StateHandler =>
      val finiteDuration = fd"${handler.checkInterval()}"
      cancellable.cancel
      log.info("receive schedule check state request from [{}], start schedule with duration [{}]", sender, finiteDuration)
      val cancelable: Cancellable = context.system.scheduler.schedule(finiteDuration, finiteDuration) {
        self.tell(CheckState, client.client)
      }
      context.become(receive(hook, cancelable, client))
  }

  private def caseKillAction(implicit hook: ExecutorHook, cancellable: Cancellable, client: ClientHolder): Receive = {
    case KillAction =>
      hook.kill()
      self ! KillMessage("")
  }

  private def caseTerminateAction(implicit hook: ExecutorHook, cancellable: Cancellable, client: ClientHolder): Receive = {
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

  private def casePrepare(implicit hook: ExecutorHook, cancellable: Cancellable, client: ClientHolder): Receive = {
    case _: PrepareMessage =>
      log.info("receive status changing, status: PREPARE")
      context.become(prepare)
      checkState
  }
  private def caseStart(implicit hook: ExecutorHook, cancellable: Cancellable, client: ClientHolder): Receive = {
    case _: StartMessage =>
      log.info("receive status changing, status: START")
      context.become(start)
      checkState
  }
  private def caseRunning(implicit hook: ExecutorHook, cancellable: Cancellable, client: ClientHolder): Receive = {
    case _: RunningMessage =>
      log.info("receive status changing, status: RUNNING")
      context.become(running)
      checkState
  }
  private def caseSuccess(implicit hook: ExecutorHook, cancellable: Cancellable, client: ClientHolder): Receive = {
    case _: SuccessMessage =>
      log.info("receive status changing, status: SUCCESS")
      context.become(success)
      checkState
  }
  private def caseFailed(implicit hook: ExecutorHook, cancellable: Cancellable, client: ClientHolder): Receive = {
    case _: FailedMessage =>
      log.info("receive status changing, status: FAILED")
      context.become(failed)
      checkState
  }
  private def caseRetry(implicit hook: ExecutorHook, cancellable: Cancellable, client: ClientHolder): Receive = {
    case _: RetryMessage =>
      log.info("receive status changing, status: RETRY")
      context.become(retry)
      checkState
  }
  private def caseKill(implicit hook: ExecutorHook, cancellable: Cancellable, client: ClientHolder): Receive = {
    case _: KillMessage =>
      log.info("receive status changing, status: KILL")
      context.become(kill)
      checkState
  }
  private def caseTimeout(implicit hook: ExecutorHook, cancellable: Cancellable, client: ClientHolder): Receive = {
    case _: TimeoutMessage =>
      log.info("receive status changing, status: TIMEOUT")
      context.become(timeout)
      checkState
  }

  private def checkState(implicit client: ClientHolder): Unit = {
    self.tell(CheckState, client.client)
  }
}