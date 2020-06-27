package com.oceanum.common

import akka.actor.ActorRef
import com.oceanum.client.{StateHandler, Task}

trait Message {}
@SerialVersionUID(11111000L)
case class PrepareMessage(message: Any) extends Message
@SerialVersionUID(11111001L)
case class RunningMessage(message: Any) extends Message
@SerialVersionUID(11111002L)
case class FailedMessage(message: Any) extends Message
@SerialVersionUID(11111003L)
case class SuccessMessage(message: Any) extends Message
@SerialVersionUID(11111004L)
case class RetryMessage(message: Any) extends Message
@SerialVersionUID(11111005L)
case class KillMessage(message: Any) extends Message
@SerialVersionUID(11111006L)
case class TimeoutMessage(message: Any) extends Message
@SerialVersionUID(11111007L)
case class StartMessage(message: Any) extends Message

@SerialVersionUID(11111008L)
case object KillAction extends Message
@SerialVersionUID(11111009L)
case object TerminateAction extends Message
@SerialVersionUID(11111010L)
case object CheckState extends Message

@SerialVersionUID(11111011L)
case class AvailableExecutorRequest(topic: String) extends Message
@SerialVersionUID(11111012L)
case class AvailableExecutorsRequest(topic: String, maxWait: String) extends Message
@SerialVersionUID(11111013L)
case class AvailableExecutorResponse(executor: TraversableOnce[AvailableExecutor]) extends Message
@SerialVersionUID(11111014L)
case class AvailableExecutor(actor: ActorRef, queueSize: Int, topics: Seq[String]) extends Message
@SerialVersionUID(11111015L)
case class ExecuteOperatorRequest(operatorMessage: Task, stateHandler: StateHandler) extends Message
@SerialVersionUID(11111016L)
case class ExecuteOperatorResponse(operatorMessage: Task, stateHandler: StateHandler) extends Message
@SerialVersionUID(11111017L)
case class HandleState(handler: StateHandler) extends Message
@SerialVersionUID(11111018L)
case class HandleOnComplete(handler: StateHandler) extends Message

@SerialVersionUID(11111019L)
case object ClusterMetricsRequest extends Message
@SerialVersionUID(11111020L)
case object ClusterInfoRequest extends Message

@SerialVersionUID(11111021L)
case object Ping extends Message
@SerialVersionUID(11111022L)
case object Pong extends Message
