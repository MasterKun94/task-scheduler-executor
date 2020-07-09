package com.oceanum.cluster

import java.util.Date

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable, PoisonPill}
import com.oceanum.common.Implicits._
import com.oceanum.client.{StateHandler, Task, TaskMeta}
import com.oceanum.cluster.exec.{EventListener, Hook, Operator, OperatorTask, RunnerManager, State}
import com.oceanum.cluster.exec.State._
import com.oceanum.common.Scheduler.{schedule, scheduleOnce}
import com.oceanum.common._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
/**
 * @author chenmingkun
 * @date 2020/5/2
 */
class ExecutionInstance(task: Task, stateHandler: StateHandler, actor: ActorRef) extends Actor with ActorLogging {
  case class Start(operator: Operator[_ <: OperatorTask], client: ActorRef, handler: StateHandler)

  private def listener(initMeta: TaskMeta): EventListener = new EventListener {
    override def prepare(message: TaskMeta): Unit = self ! PrepareMessage(initMeta ++ message)
    override def start(message: TaskMeta): Unit = self ! StartMessage(initMeta ++ message)
    override def running(message: TaskMeta): Unit = self ! RunningMessage(initMeta ++ message)
    override def failed(message: TaskMeta): Unit = self ! FailedMessage(initMeta ++ message)
    override def success(message: TaskMeta): Unit = self ! SuccessMessage(initMeta ++ message)
    override def retry(message: TaskMeta): Unit = self ! RetryMessage(initMeta ++ message)
    override def timeout(message: TaskMeta): Unit = self ! TimeoutMessage(initMeta ++ message)
    override def kill(message: TaskMeta): Unit = self ! KillMessage(initMeta ++ message)
  }

  var execTimeoutMax: Cancellable = _

  override def preStart(): Unit = {
    implicit val executor: ExecutionContext = Environment.GLOBAL_EXECUTOR
    task.init(listener).onComplete {
      case Success(operator) =>
        self ! Start(operator, actor, stateHandler)
      case Failure(e) =>
        e.printStackTrace()
        implicit val hook: Hook = new Hook {
          override def kill(): Boolean = true
          override def isKilled: Boolean = true
        }
        implicit val cancellable: Cancellable = new Cancellable {
          override def cancel(): Boolean = true
          override def isCancelled: Boolean = true
        }
        implicit val clientHolder: ClientHolder = ClientHolder(actor)
        context.become(failed(task.metadata + ("message" -> e.getMessage)))
    }
    execTimeoutMax = scheduleOnce(Environment.EXEC_MAX_TIMEOUT) {
      self ! PoisonPill
    }
  }

  override def postStop(): Unit = execTimeoutMax.cancel()

  override def receive: Receive = {
    case Start(operator, client, handler) =>
      val duration = fd"${handler.checkInterval()}"
      log.info("receive operator: [{}], receive schedule check state request from [{}], start schedule with duration [{}]", operator, sender, duration)
      implicit val cancelable: Cancellable = schedule(duration, duration) {
        self.tell(CheckState, client)
      }
      implicit val clientHolder: ClientHolder = ClientHolder(client)
      implicit val hook: Hook = RunnerManager.submit(operator)
      context.become(offline(operator.metadata))
      client ! ExecuteOperatorResponse(operator.metadata, handler)
  }

  type Params = (State, (Hook, Cancellable, ClientHolder) => Receive)
  private def offline_(metadata: TaskMeta) : Params = (OFFLINE(metadata), offline(metadata)(_, _, _))
  private def prepare_(metadata: TaskMeta) : Params  = (PREPARE(metadata), prepare(metadata)(_, _, _))
  private def start_(metadata: TaskMeta) : Params  = (START(metadata), start(metadata)(_, _, _))
  private def running_(metadata: TaskMeta) : Params  = (RUNNING(metadata), running(metadata)(_, _, _))
  private def retry_(metadata: TaskMeta) : Params  = (RETRY(metadata), retry(metadata)(_, _, _))
  private def timeout_(metadata: TaskMeta) : Params  = (TIMEOUT(metadata), timeout(metadata)(_, _, _))
  private def success_(metadata: TaskMeta) : Params  = (SUCCESS(metadata), success(metadata)(_, _, _))
  private def failed_(metadata: TaskMeta) : Params  = (FAILED(metadata), failed(metadata)(_, _, _))
  private def kill_(metadata: TaskMeta) : Params  = (KILL(metadata), kill(metadata)(_, _, _))
  case class ClientHolder(client: ActorRef)

  private def offline(metadata: TaskMeta)(implicit hook: Hook, cancellable: Cancellable, client: ClientHolder): Receive = {
    caseCheckState(offline_(metadata))
      .orElse(caseKillAction)
      .orElse(casePrepare)
      .orElse(caseKill)
  }

  private def prepare(metadata: TaskMeta)(implicit hook: Hook, cancellable: Cancellable, client: ClientHolder): Receive = {
    caseCheckState(prepare_(metadata))
      .orElse(caseKillAction)
      .orElse(caseStart)
      .orElse(caseKill)
  }

  private def start(metadata: TaskMeta)(implicit hook: Hook, cancellable: Cancellable, client: ClientHolder): Receive = {
    caseCheckState(start_(metadata))
      .orElse(caseKillAction)
      .orElse(caseFailed)
      .orElse(caseRunning)
      .orElse(caseKill)
  }

  private def running(metadata: TaskMeta)(implicit hook: Hook, cancellable: Cancellable, client: ClientHolder): Receive = {
    caseCheckState(running_(metadata))
      .orElse(caseKillAction)
      .orElse(caseSuccess)
      .orElse(caseFailed)
      .orElse(caseRetry)
      .orElse(caseTimeout)
      .orElse(caseKill)
  }

  private def retry(metadata: TaskMeta)(implicit hook: Hook, cancellable: Cancellable, client: ClientHolder): Receive = {
    caseCheckState(retry_(metadata))
      .orElse(caseKillAction)
      .orElse(casePrepare)
      .orElse(caseKill)
  }

  private def timeout(metadata: TaskMeta)(implicit hook: Hook, cancellable: Cancellable, client: ClientHolder): Receive = {
    caseCheckState(timeout_(metadata))
      .orElse(caseKillAction)
      .orElse(caseRetry)
      .orElse(caseFailed)
      .orElse(caseKill)
  }

  private def success(metadata: TaskMeta)(implicit hook: Hook, cancellable: Cancellable, client: ClientHolder): Receive = {
    caseCheckState(success_(metadata))
      .orElse(caseTerminateAction)
  }

  private def failed(metadata: TaskMeta)(implicit hook: Hook, cancellable: Cancellable, client: ClientHolder): Receive = {
    caseCheckState(failed_(metadata))
      .orElse(caseTerminateAction)
  }

  private def kill(metadata: TaskMeta)(implicit hook: Hook, cancellable: Cancellable, client: ClientHolder): Receive = {
    caseCheckState(kill_(metadata))
      .orElse(caseTerminateAction)
      .orElse(caseFailed)
  }

  private def caseCheckState(stateReceive: Params)(implicit hook: Hook, cancellable: Cancellable, client: ClientHolder): Receive = {
    case CheckState =>
      val state = stateReceive._1
      log.info("send state: [{}] to sender: [{}]", state, sender)
      sender ! state
    case handler: StateHandler =>
      val receive = stateReceive._2
      val finiteDuration = fd"${handler.checkInterval()}"
      cancellable.cancel
      log.info("receive schedule check state request from [{}], start schedule with duration [{}]", sender, finiteDuration)
      val cancelable: Cancellable = schedule(finiteDuration, finiteDuration) {
        self.tell(CheckState, client.client)
      }
      context.become(receive(hook, cancelable, client))
  }

  private def caseKillAction(implicit hook: Hook, cancellable: Cancellable, client: ClientHolder): Receive = {
    case KillAction =>
      hook.kill()
      self ! KillMessage(TaskMeta.empty)
  }

  private def caseTerminateAction(implicit hook: Hook, cancellable: Cancellable, client: ClientHolder): Receive = {
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

  private def casePrepare(implicit hook: Hook, cancellable: Cancellable, client: ClientHolder): Receive = {
    case m: PrepareMessage =>
      log.info("receive status changing, status: PREPARE")
      context.become(prepare(m.metadata))
      checkState
  }
  private def caseStart(implicit hook: Hook, cancellable: Cancellable, client: ClientHolder): Receive = {
    case m: StartMessage =>
      log.info("receive status changing, status: START")
      context.become(start(m.metadata.startTime = new Date()))
      checkState
  }
  private def caseRunning(implicit hook: Hook, cancellable: Cancellable, client: ClientHolder): Receive = {
    case m: RunningMessage =>
      log.info("receive status changing, status: RUNNING")
      context.become(running(m.metadata))
      checkState
  }
  private def caseSuccess(implicit hook: Hook, cancellable: Cancellable, client: ClientHolder): Receive = {
    case m: SuccessMessage =>
      log.info("receive status changing, status: SUCCESS")
      context.become(success(m.metadata.endTime = new Date()))
      checkState
  }
  private def caseFailed(implicit hook: Hook, cancellable: Cancellable, client: ClientHolder): Receive = {
    case m: FailedMessage =>
      log.info("receive status changing, status: FAILED")
      context.become(failed(m.metadata.endTime = new Date()))
      checkState
  }
  private def caseRetry(implicit hook: Hook, cancellable: Cancellable, client: ClientHolder): Receive = {
    case m: RetryMessage =>
      log.info("receive status changing, status: RETRY")
      context.become(retry(m.metadata))
      checkState
  }
  private def caseKill(implicit hook: Hook, cancellable: Cancellable, client: ClientHolder): Receive = {
    case m: KillMessage =>
      log.info("receive status changing, status: KILL")
      context.become(kill(m.metadata.endTime = new Date()))
      checkState
  }
  private def caseTimeout(implicit hook: Hook, cancellable: Cancellable, client: ClientHolder): Receive = {
    case m: TimeoutMessage =>
      log.info("receive status changing, status: TIMEOUT")
      context.become(timeout(m.metadata.endTime = new Date()))
      checkState
  }

  private def checkState(implicit client: ClientHolder): Unit = {
    self.tell(CheckState, client.client)
  }
}