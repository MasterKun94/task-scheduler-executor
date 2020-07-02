package com.oceanum.client.actors

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.oceanum.client.{StateHandler, TaskInstance}
import com.oceanum.cluster.exec.State
import com.oceanum.cluster.exec.State.{FAILED, KILL, SUCCESS}
import com.oceanum.common._

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
      context.become(onRunning(sender(), res.stateHandler))
  }

  def onRunning(executor: ActorRef, stateHandler: StateHandler): Receive = {
    case CheckState =>
      log.info("send check stat to [{}]", executor)
      executor ! CheckState

    case handler: StateHandler =>
      log.info("send schedule check stat to [{}]", executor)
      executor ! handler
      context.become(onRunning(executor, handler))
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
          case KILL(_) | SUCCESS(_) | FAILED(_) => handler.handle(state)
          case _ =>
        }
      }
      context.become(onRunning(executor, StateHandler(newHandler)))

    case stat: State =>
      log.info("receive stat from [{}], state: {}", sender(), stat)
      stateHandler.handle(stat)
  }
}
