package com.oceanum.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable}
import akka.cluster.client.ClusterClient.Publish
import com.oceanum.client._
import com.oceanum.common.Environment
import com.oceanum.exec.EventListener
import com.oceanum.exec.EventListener._

import scala.concurrent.ExecutionContext

/**
 * @author chenmingkun
 * @date 2020/5/3
 */
class ClientActor(clusterClient: ActorRef, executionContext: ExecutionContext) extends Actor with ActorLogging {
  implicit private val ec: ExecutionContext = executionContext

  override def preStart(): Unit = {
    context.system.scheduler.scheduleOnce(Environment.ACTOR_ALIVE_TIME_MAX) {
      context.stop(self)
    }
  }

  override def receive: Receive = {
    case req: AvailableExecutorsRequest =>
      clusterClient ! Publish(req.topic, AvailableExecutorRequest(req.topic))
      context.become(receiveExecutors(sender(), Array.empty))
      context.system.scheduler.scheduleOnce(req.maxWait) {
        self ! req
      }

    case req: AvailableExecutorRequest =>
      clusterClient ! Publish(req.topic, req)
      context.become(receiveExecutor(sender()))
  }

  def receiveExecutors(receiver: ActorRef, executors: Array[AvailableExecutor]): Receive = {
    case executor: AvailableExecutor =>
      context.become(receiveExecutors(receiver, executors :+ executor))

    case req: AvailableExecutorsRequest =>
      receiver ! AvailableExecutorResponse(executors)
      context.stop(self)
      cancelable.cancel()
  }

  def receiveExecutor(receiver: ActorRef): Receive = {
    case executor: AvailableExecutor =>
      receiver ! AvailableExecutorResponse(Some(executor))
      context.stop(self)
      cancelable.cancel()
  }
}

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
      actor ! ExecutorInstance(Seq(self))
      context.become(onRunning(sender(), res.checkStateScheduled.handler))
  }

  def onRunning(executor: ActorRef, stateHandler: StateHandler): Receive = {
    case CheckStateOnce =>
      log.info("send check stat from [{}], to [{}]", sender(), executor)
      executor ! CheckStateOnce

    case scheduleCheckState: CheckStateScheduled =>
      log.info("send schedule check stat from [{}], to [{}]", sender(), executor)
      executor ! scheduleCheckState
      context.become(onRunning(executor, scheduleCheckState.handler))
      sender() ! "OK"

    case KillAction =>
      log.info("send kill action from [{}], to [{}]", sender(), executor)
      executor ! KillAction
      sender() ! "OK"

    case TerminateAction =>
      log.info("terminating [{}] and [{}]", sender(), executor)
      executor ! TerminateAction
      context.stop(self)
      sender() ! "OK"

    case HandleState(handler) =>
      context.become(onRunning(executor, handler))
      sender() ! "OK"

    case HandleOnComplete(handler) =>
      val newHandler: EventListener.State => Unit = state => {
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
