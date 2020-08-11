package com.oceanum.cluster

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable, PoisonPill}
import com.oceanum.client.Task
import com.oceanum.common.Scheduler.{schedule, scheduleOnce}
import com.oceanum.common.{TaskStatus, _}
import com.oceanum.exec.{EventListener, ExecutionHook, ExecutionTask, FAILED, KILL, OFFLINE, PREPARE, RETRY, RUNNING, RunnerManager, START, SUCCESS, State, TIMEOUT}

import scala.concurrent.ExecutionContext

/**
 * @author chenmingkun
 * @date 2020/5/2
 */
class ExecutionInstance(task: Task, actor: ActorRef) extends Actor with ActorLogging {
  private def listener: EventListener = new EventListener {
    override def prepare(message: RichTaskMeta => RichTaskMeta): Unit = {
      self ! PrepareMessage(message)
    }

    override def start(message: RichTaskMeta => RichTaskMeta): Unit = {
      self ! StartMessage(message)
    }

    override def running(message: RichTaskMeta => RichTaskMeta): Unit = {
      self ! RunningMessage(message)
    }

    override def failed(message: RichTaskMeta => RichTaskMeta): Unit = {
      self ! FailedMessage(message)
    }

    override def success(message: RichTaskMeta => RichTaskMeta): Unit = {
      self ! SuccessMessage(message)
    }

    override def retry(message: RichTaskMeta => RichTaskMeta): Unit = {
      self ! RetryMessage(message)
    }

    override def timeout(message: RichTaskMeta => RichTaskMeta): Unit = {
      self ! TimeoutMessage(message)
    }

    override def kill(message: RichTaskMeta => RichTaskMeta): Unit = {
      self ! KillMessage
    }
  }
  private val executionTask = ExecutionTask.from(task, listener)
  private val client = actor
  var execTimeoutMax: Cancellable = _
  var clientCheckTime: Long = System.currentTimeMillis()

  override def preStart(): Unit = {
    val interval = task.checkStateInterval
    log.info("receive operator: [{}], receive schedule check state request from [{}], start schedule with duration [{}]", executionTask, sender, interval)
    val cancelable: Cancellable = schedule(interval, interval) {
      self.tell(CheckState, client)
    }
    val hook: ExecutionHook = RunnerManager.submit(executionTask)
    val metadata: RichTaskMeta = executionTask.metadata
    context.become(offline(metadata)(Holder(hook, cancelable, client)))
    client ! ExecuteOperatorResponse(executionTask.metadata)
    execTimeoutMax = scheduleOnce(Environment.EXEC_MAX_TIMEOUT) {
      context.stop(self)
    }
  }

  override def postStop(): Unit = execTimeoutMax.cancel()

  override def receive: Receive = { case _ => }

  private def offline(meta: RichTaskMeta)(implicit holder: Holder): Receive = {
    implicit val metadata: RichTaskMeta = meta
    caseCheckState(offline_)
      .orElse(caseKillAction)
      .orElse(casePrepare)
      .orElse(caseKill)
  }

  private def prepare(meta: RichTaskMeta)(implicit holder: Holder): Receive = {
    implicit val metadata: RichTaskMeta = meta
    caseCheckState(prepare_)
      .orElse(caseKillAction)
      .orElse(caseStart)
      .orElse(caseKill)
  }

  private def start(meta: RichTaskMeta)(implicit holder: Holder): Receive = {
    implicit val metadata: RichTaskMeta = meta
    caseCheckState(start_)
      .orElse(caseKillAction)
      .orElse(caseFailed)
      .orElse(caseRunning)
      .orElse(caseRetry)
      .orElse(caseKill)
  }

  private def running(meta: RichTaskMeta)(implicit holder: Holder): Receive = {
    implicit val metadata: RichTaskMeta = meta
    caseCheckState(running_)
      .orElse(caseRunning)
      .orElse(caseKillAction)
      .orElse(caseSuccess)
      .orElse(caseFailed)
      .orElse(caseRetry)
      .orElse(caseTimeout)
      .orElse(caseKill)
  }

  private def retry(meta: RichTaskMeta)(implicit holder: Holder): Receive = {
    implicit val metadata: RichTaskMeta = meta
    caseCheckState(retry_)
      .orElse(caseKillAction)
      .orElse(casePrepare)
      .orElse(caseKill)
  }

  private def timeout(meta: RichTaskMeta)(implicit holder: Holder): Receive = {
    implicit val metadata: RichTaskMeta = meta
    caseCheckState(timeout_)
      .orElse(caseKillAction)
      .orElse(caseRetry)
      .orElse(caseFailed)
      .orElse(caseKill)
  }

  private def success(meta: RichTaskMeta)(implicit holder: Holder): Receive = {
    implicit val metadata: RichTaskMeta = meta
    caseCheckState(success_)
      .orElse(caseTerminateAction)
  }

  private def failed(meta: RichTaskMeta)(implicit holder: Holder): Receive = {
    implicit val metadata: RichTaskMeta = meta
    caseCheckState(failed_)
      .orElse(caseTerminateAction)
  }

  private def kill(meta: RichTaskMeta)(implicit holder: Holder): Receive = {
    implicit val metadata: RichTaskMeta = meta
    caseCheckState(kill_)
      .orElse(caseTerminateAction)
      .orElse(caseFailed)
  }

  private def caseCheckState(stateReceive: StateReceive)(implicit holder: Holder): Receive = {
    case CheckState =>
      log.debug("send state: [{}] to sender: [{}]", stateReceive.state.name, sender)
      sender ! stateReceive.state
      if ((System.currentTimeMillis() - clientCheckTime) > Environment.EXEC_UN_REACH_TIMEOUT
        && Array(TaskStatus.SUCCESS, TaskStatus.KILL, TaskStatus.FAILED).contains(stateReceive.state.name)) {
        self ! TerminateAction
      }

    case Pong =>
      clientCheckTime = System.currentTimeMillis()
  }

  private def caseKillAction(implicit holder: Holder): Receive = {
    case KillAction =>
      holder.hook.kill()
      self ! KillMessage
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

  private def casePrepare(implicit meta: RichTaskMeta, holder: Holder): Receive = {
    case m: PrepareMessage =>
      val metadata = m.message(meta)
      log.info("receive status changing, status: PREPARE({})", metadata)
      context.become(prepare(metadata))
      checkState
  }
  private def caseStart(implicit meta: RichTaskMeta, holder: Holder): Receive = {
    case m: StartMessage =>
      val metadata = m.message(meta)
      log.info("receive status changing, status: START({})", metadata)
      context.become(start(metadata))
      checkState
  }
  private def caseRunning(implicit meta: RichTaskMeta, holder: Holder): Receive = {
    case m: RunningMessage =>
      val metadata = m.message(meta)
      log.info("receive status changing, status: RUNNING({})", metadata)
      context.become(running(metadata))
      checkState
  }
  private def caseSuccess(implicit meta: RichTaskMeta, holder: Holder): Receive = {
    case m: SuccessMessage =>
      val metadata = m.message(meta)
      log.info("receive status changing, status: SUCCESS({})", metadata)
      context.become(success(metadata))
      checkState
  }
  private def caseFailed(implicit meta: RichTaskMeta, holder: Holder): Receive = {
    case m: FailedMessage =>
      val metadata = m.message(meta)
      log.info("receive status changing, status: FAILED({})", metadata)
      context.become(failed(metadata))
      checkState
  }
  private def caseRetry(implicit meta: RichTaskMeta, holder: Holder): Receive = {
    case m: RetryMessage =>
      val metadata = m.message(meta)
      log.info("receive status changing, status: RETRY({})", metadata)
      context.become(retry(metadata))
      checkState
  }
  private def caseKill(implicit meta: RichTaskMeta, holder: Holder): Receive = {
    case KillMessage =>
      val metadata = meta
      log.info("receive status changing, status: KILL({})", metadata)
      context.become(kill(metadata))
      checkState
  }
  private def caseTimeout(implicit meta: RichTaskMeta, holder: Holder): Receive = {
    case m: TimeoutMessage =>
      val metadata = m.message(meta)
      log.info("receive status changing, status: TIMEOUT({})", metadata)
      context.become(timeout(metadata))
      checkState
  }

  private def checkState(implicit holder: Holder): Unit = {
    self.tell(CheckState, holder.client)
  }

  case class Holder(hook: ExecutionHook, cancellable: Cancellable, client: ActorRef)
  case class StateReceive(state: State, receive: Holder => Receive)
  private def offline_(implicit metadata: RichTaskMeta) : StateReceive = StateReceive(OFFLINE(metadata), offline(metadata)(_))
  private def prepare_(implicit metadata: RichTaskMeta) : StateReceive  = StateReceive(PREPARE(metadata), prepare(metadata)(_))
  private def start_(implicit metadata: RichTaskMeta) : StateReceive  = StateReceive(START(metadata), start(metadata)(_))
  private def running_(implicit metadata: RichTaskMeta) : StateReceive  = StateReceive(RUNNING(metadata), running(metadata)(_))
  private def retry_(implicit metadata: RichTaskMeta) : StateReceive  = StateReceive(RETRY(metadata), retry(metadata)(_))
  private def timeout_(implicit metadata: RichTaskMeta) : StateReceive  = StateReceive(TIMEOUT(metadata), timeout(metadata)(_))
  private def success_(implicit metadata: RichTaskMeta) : StateReceive  = StateReceive(SUCCESS(metadata), success(metadata)(_))
  private def failed_(implicit metadata: RichTaskMeta) : StateReceive  = StateReceive(FAILED(metadata), failed(metadata)(_))
  private def kill_(implicit metadata: RichTaskMeta) : StateReceive  = StateReceive(KILL(metadata), kill(metadata)(_))

}