package com.oceanum.cluster

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.oceanum.api.TaskInstance
import com.oceanum.exec.State._

class ClientExecutor(executor: ActorRef) extends Actor with ActorLogging {

  override def receive: Receive = {
    case req: ExecuteOperatorRequest =>
      executor ! req
      context.become(prepareRunning(sender()))
  }

  def prepareRunning(actor: ActorRef = null): Receive = {
    case req: ExecuteOperatorRequest =>
      executor ! req
      context.become(prepareRunning(sender()))

    case res: ExecuteOperatorResponse =>
      log.info("receive operator response from " + sender())
      actor ! TaskInstance(Seq(self))
      context.become(onRunning(sender(), res.checkStateScheduled.handler))
  }

  def onRunning(executor: ActorRef, stateHandler: StateHandler): Receive = {
    case CheckStateOnce =>
      log.info("send check stat to [{}]", executor)
      executor ! CheckStateOnce

    case scheduleCheckState: CheckStateScheduled =>
      log.info("send schedule check stat to [{}]", executor)
      executor ! scheduleCheckState
      context.become(onRunning(executor, scheduleCheckState.handler))
      sender() ! "OK"

    case KillAction =>
      log.info("send kill action to [{}]", executor)
      executor ! KillAction
      sender() ! "OK"

    case TerminateAction =>
      log.info("terminating [{}]", executor)
      executor ! TerminateAction
      context.stop(self)
      sender() ! "OK"

    case HandleState(handler) =>
      context.become(onRunning(executor, handler))
      sender() ! "OK"

    case HandleOnComplete(handler) =>
      val newHandler: State => Unit = state => {
        stateHandler.handle(state)
        state match {
          case KILL | SUCCESS | FAILED => handler(state)
          case _ =>
        }
      }
      context.become(onRunning(executor, StateHandler(newHandler)))

    case stat: State =>
      log.info("receive stat from [{}], state: {}", sender(), stat)
      stateHandler(stat)
  }
}
