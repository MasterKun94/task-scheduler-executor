package com.oceanum.client.actors

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill}
import com.oceanum.client.{StateHandler, Task}
import com.oceanum.cluster.exec.State
import com.oceanum.cluster.exec.State.{FAILED, KILL, SUCCESS}
import com.oceanum.common._

import scala.concurrent.Promise

class ClientInstance(executor: ActorRef, task: Task, promise: Promise[State]) extends Actor with ActorLogging {

  override def preStart(): Unit = {
    executor ! ExecuteOperatorRequest(task)
  }

  override def receive: Receive = {
    case res: ExecuteOperatorResponse =>
      log.info("receive operator response from " + sender())
      context.become(onRunning(sender(), task.stateHandler))
  }

//  def prepareRunning(actor: ActorRef = null): Receive = {
//    case req: ExecuteOperatorRequest =>
//      executor ! req
//      context.become(prepareRunning(sender()))
//
//    case res: ExecuteOperatorResponse =>
//      log.info("receive operator response from " + sender())
//      actor ! TaskInstance(Seq(self))
//      context.become(onRunning(sender(), res.stateHandler))
//  }

  def onRunning(executor: ActorRef, stateHandler: StateHandler): Receive = {

    case KillAction =>
      log.info("send kill action to [{}]", executor)
      executor ! KillAction
      sender() ! "OK"

    case TerminateAction =>
      log.info("terminating [{}]", executor)
      executor ! TerminateAction
      self ! PoisonPill
      sender() ! "OK"

    case stat: State =>
      log.info("receive stat from [{}], state: {}", sender(), stat)
      stateHandler.handle(stat)
      stat match {
        case KILL(_) | SUCCESS(_) | FAILED(_) =>
          promise.success(stat)
          self ! TerminateAction
        case _ =>
      }
  }
}
