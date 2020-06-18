package com.oceanum.cluster

import akka.actor.ActorRef
import com.oceanum.api.Task

import scala.concurrent.duration.FiniteDuration

case class PrepareMessage(message: Any)
case class RunningMessage(message: Any)
case class FailedMessage(message: Any)
case class SuccessMessage(message: Any)
case class RetryMessage(message: Any)
case class KillMessage(message: Any)
case class TimeoutMessage(message: Any)
case class StartMessage(message: Any)

case object KillAction
case object TerminateAction
case object CheckStateOnce
case class CheckStateScheduled(duration: FiniteDuration, handler: StateHandler = StateHandler.empty())

case class AvailableExecutorRequest(topic: String)
case class AvailableExecutorsRequest(topic: String, maxWait: FiniteDuration)
case class AvailableExecutorResponse(executor: TraversableOnce[AvailableExecutor])
case class AvailableExecutor(actor: ActorRef, queueSize: Int, topics: Array[String])
case class ExecuteOperatorRequest(operatorMessage: Task, checkStateScheduled: CheckStateScheduled)
case class ExecuteOperatorResponse(operatorMessage: Task, checkStateScheduled: CheckStateScheduled)
case class HandleState(handler: StateHandler)
case class HandleOnComplete(handler: StateHandler)
