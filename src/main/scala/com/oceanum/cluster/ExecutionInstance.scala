package com.oceanum.cluster

import java.util.Date

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable, PoisonPill}
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

  implicit private val listenerGenerator: TaskMeta => EventListener = meta => new EventListener {
    override def prepare(message: TaskMeta): Unit = self ! PrepareMessage(meta ++ message)
    override def start(message: TaskMeta): Unit = self ! StartMessage(meta ++ message)
    override def running(message: TaskMeta): Unit = self ! RunningMessage(meta ++ message)
    override def failed(message: TaskMeta): Unit = self ! FailedMessage(meta ++ message)
    override def success(message: TaskMeta): Unit = self ! SuccessMessage(meta ++ message)
    override def retry(message: TaskMeta): Unit = self ! RetryMessage(meta ++ message)
    override def timeout(message: TaskMeta): Unit = self ! TimeoutMessage(meta ++ message)
    override def kill(message: TaskMeta): Unit = self ! KillMessage(meta ++ message)
  }

  var execTimeoutMax: Cancellable = _

  override def preStart(): Unit = {
    implicit val executor: ExecutionContext = Environment.GLOBAL_EXECUTOR
    task.init.onComplete {
      case Success(operator) =>
        self ! Start(operator, actor, stateHandler)
      case Failure(e) =>
        e.printStackTrace()
        val cancellable: Cancellable = Cancellable.alreadyCancelled
        val hook: Hook = Hook(cancellable)
        context.become(failed(task.metadata.message = e.getMessage)(Holder(hook, cancellable, actor)))
    }
    execTimeoutMax = scheduleOnce(Environment.EXEC_MAX_TIMEOUT) {
      context.stop(self)
    }
  }

  override def postStop(): Unit = execTimeoutMax.cancel()

  override def receive: Receive = {
    case Start(operator, client, handler) =>
      val duration = handler.checkInterval()
      log.info("receive operator: [{}], receive schedule check state request from [{}], start schedule with duration [{}]", operator, sender, duration)
      val cancelable: Cancellable = schedule(duration, duration) {
        self.tell(CheckState, client)
      }
      val hook: Hook = RunnerManager.submit(operator)
      val metadata: TaskMeta = operator.metadata
      context.become(offline(metadata)(Holder(hook, cancelable, client)))
      client ! ExecuteOperatorResponse(operator.metadata, handler)
  }

  private def offline(meta: TaskMeta)(implicit holder: Holder): Receive = {
    implicit val metadata: TaskMeta = meta.createTime = new Date()
    caseCheckState(offline_)
      .orElse(caseKillAction)
      .orElse(casePrepare)
      .orElse(caseKill)
  }

  private def prepare(meta: TaskMeta)(implicit holder: Holder): Receive = {
    implicit val metadata: TaskMeta = meta
    caseCheckState(prepare_)
      .orElse(caseKillAction)
      .orElse(caseStart)
      .orElse(caseKill)
  }

  private def start(meta: TaskMeta)(implicit holder: Holder): Receive = {
    implicit val metadata: TaskMeta = meta.startTime = new Date()
    caseCheckState(start_)
      .orElse(caseKillAction)
      .orElse(caseFailed)
      .orElse(caseRunning)
      .orElse(caseKill)
  }

  private def running(meta: TaskMeta)(implicit holder: Holder): Receive = {
    implicit val metadata: TaskMeta = meta
    caseCheckState(running_)
      .orElse(caseKillAction)
      .orElse(caseSuccess)
      .orElse(caseFailed)
      .orElse(caseRetry)
      .orElse(caseTimeout)
      .orElse(caseKill)
  }

  private def retry(meta: TaskMeta)(implicit holder: Holder): Receive = {
    implicit val metadata: TaskMeta = meta.incRetry()
    caseCheckState(retry_)
      .orElse(caseKillAction)
      .orElse(casePrepare)
      .orElse(caseKill)
  }

  private def timeout(meta: TaskMeta)(implicit holder: Holder): Receive = {
    implicit val metadata: TaskMeta = meta
    caseCheckState(timeout_)
      .orElse(caseKillAction)
      .orElse(caseRetry)
      .orElse(caseFailed)
      .orElse(caseKill)
  }

  private def success(meta: TaskMeta)(implicit holder: Holder): Receive = {
    implicit val metadata: TaskMeta = meta.endTime = new Date()
    caseCheckState(success_)
      .orElse(caseTerminateAction)
  }

  private def failed(meta: TaskMeta)(implicit holder: Holder): Receive = {
    implicit val metadata: TaskMeta = meta.endTime = new Date()
    caseCheckState(failed_)
      .orElse(caseTerminateAction)
  }

  private def kill(meta: TaskMeta)(implicit holder: Holder): Receive = {
    implicit val metadata: TaskMeta = meta.endTime = new Date()
    caseCheckState(kill_)
      .orElse(caseTerminateAction)
      .orElse(caseFailed)
  }

  private def caseCheckState(stateReceive: StateReceive)(implicit holder: Holder): Receive = {
    case CheckState =>
      log.info("send state: [{}] to sender: [{}]", stateReceive.state, sender)
      sender ! stateReceive.state
    case handler: StateHandler =>
      val finiteDuration = handler.checkInterval()
      holder.cancellable.cancel
      log.info("receive schedule check state request from [{}], start schedule with duration [{}]", sender, finiteDuration)
      val cancelable: Cancellable = schedule(finiteDuration, finiteDuration) {
        self.tell(CheckState, holder.client)
      }
      context.become(stateReceive.receive(holder.copy(cancellable = cancelable)))
  }

  private def caseKillAction(implicit holder: Holder): Receive = {
    case KillAction =>
      holder.hook.kill()
      self ! KillMessage(TaskMeta.empty)
  }

  private def caseTerminateAction(implicit holder: Holder): Receive = {
    case TerminateAction =>
      log.info("terminate this action")
      if (holder.cancellable != null || !holder.cancellable.isCancelled) {
        holder.cancellable.cancel()
      }
      execTimeoutMax.cancel()
      if (!holder.hook.isKilled) {
        holder.hook.kill()
      }
      self ! PoisonPill
  }

  private def casePrepare(implicit meta: TaskMeta, holder: Holder): Receive = {
    case m: PrepareMessage =>
      log.info("receive status changing, status: PREPARE")
      context.become(prepare(m.metadata ++ meta))
      checkState
  }
  private def caseStart(implicit meta: TaskMeta, holder: Holder): Receive = {
    case m: StartMessage =>
      log.info("receive status changing, status: START")
      context.become(start(m.metadata ++ meta))
      checkState
  }
  private def caseRunning(implicit meta: TaskMeta, holder: Holder): Receive = {
    case m: RunningMessage =>
      log.info("receive status changing, status: RUNNING")
      context.become(running(m.metadata ++ meta))
      checkState
  }
  private def caseSuccess(implicit meta: TaskMeta, holder: Holder): Receive = {
    case m: SuccessMessage =>
      log.info("receive status changing, status: SUCCESS")
      context.become(success(m.metadata ++ meta))
      checkState
  }
  private def caseFailed(implicit meta: TaskMeta, holder: Holder): Receive = {
    case m: FailedMessage =>
      log.info("receive status changing, status: FAILED")
      context.become(failed(m.metadata ++ meta))
      checkState
  }
  private def caseRetry(implicit meta: TaskMeta, holder: Holder): Receive = {
    case m: RetryMessage =>
      log.info("receive status changing, status: RETRY")
      context.become(retry(m.metadata ++ meta))
      checkState
  }
  private def caseKill(implicit meta: TaskMeta, holder: Holder): Receive = {
    case m: KillMessage =>
      log.info("receive status changing, status: KILL")
      context.become(kill(m.metadata ++ meta))
      checkState
  }
  private def caseTimeout(implicit meta: TaskMeta, holder: Holder): Receive = {
    case m: TimeoutMessage =>
      log.info("receive status changing, status: TIMEOUT")
      context.become(timeout(m.metadata ++ meta))
      checkState
  }

  private def checkState(implicit holder: Holder): Unit = {
    self.tell(CheckState, holder.client)
  }

  case class Holder(hook: Hook, cancellable: Cancellable, client: ActorRef)
  case class StateReceive(state: State, receive: Holder => Receive)
  private def offline_(implicit metadata: TaskMeta) : StateReceive = StateReceive(OFFLINE(metadata), offline(metadata)(_))
  private def prepare_(implicit metadata: TaskMeta) : StateReceive  = StateReceive(PREPARE(metadata), prepare(metadata)(_))
  private def start_(implicit metadata: TaskMeta) : StateReceive  = StateReceive(START(metadata), start(metadata)(_))
  private def running_(implicit metadata: TaskMeta) : StateReceive  = StateReceive(RUNNING(metadata), running(metadata)(_))
  private def retry_(implicit metadata: TaskMeta) : StateReceive  = StateReceive(RETRY(metadata), retry(metadata)(_))
  private def timeout_(implicit metadata: TaskMeta) : StateReceive  = StateReceive(TIMEOUT(metadata), timeout(metadata)(_))
  private def success_(implicit metadata: TaskMeta) : StateReceive  = StateReceive(SUCCESS(metadata), success(metadata)(_))
  private def failed_(implicit metadata: TaskMeta) : StateReceive  = StateReceive(FAILED(metadata), failed(metadata)(_))
  private def kill_(implicit metadata: TaskMeta) : StateReceive  = StateReceive(KILL(metadata), kill(metadata)(_))

}