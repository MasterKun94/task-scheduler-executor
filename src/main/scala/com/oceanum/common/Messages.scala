package com.oceanum.common

import akka.actor.ActorRef
import akka.cluster.ClusterEvent.CurrentClusterState
import akka.cluster.metrics.NodeMetrics
import com.oceanum.client.{StateHandler, Task}

trait Message {}
@SerialVersionUID(1L)
case class PrepareMessage(message: Any) extends Message
@SerialVersionUID(1L)
case class RunningMessage(message: Any) extends Message
@SerialVersionUID(1L)
case class FailedMessage(message: Any) extends Message
@SerialVersionUID(1L)
case class SuccessMessage(message: Any) extends Message
@SerialVersionUID(1L)
case class RetryMessage(message: Any) extends Message
@SerialVersionUID(1L)
case class KillMessage(message: Any) extends Message
@SerialVersionUID(1L)
case class TimeoutMessage(message: Any) extends Message
@SerialVersionUID(1L)
case class StartMessage(message: Any) extends Message

@SerialVersionUID(1L)
case object KillAction extends Message
@SerialVersionUID(1L)
case object TerminateAction extends Message
@SerialVersionUID(1L)
case object CheckState extends Message

@SerialVersionUID(1L)
case class AvailableExecutorRequest(topic: String) extends Message
@SerialVersionUID(1L)
case class AvailableExecutorsRequest(topic: String, maxWait: String) extends Message
@SerialVersionUID(1L)
case class AvailableExecutorResponse(executor: TraversableOnce[AvailableExecutor]) extends Message
@SerialVersionUID(1L)
case class AvailableExecutor(actor: ActorRef, queueSize: Int, topics: Seq[String]) extends Message
@SerialVersionUID(1L)
case class ExecuteOperatorRequest(operatorMessage: Task, stateHandler: StateHandler) extends Message
@SerialVersionUID(1L)
case class ExecuteOperatorResponse(operatorMessage: Task, stateHandler: StateHandler) extends Message
@SerialVersionUID(1L)
case class HandleState(handler: StateHandler) extends Message
@SerialVersionUID(1L)
case class HandleOnComplete(handler: StateHandler) extends Message

trait ClusterInfoMessage extends Message
@SerialVersionUID(1L)
case class ClusterInfoMessageHolder(message: ClusterInfoMessage, actorRef: ActorRef) extends Message
@SerialVersionUID(1L)
case object ClusterMetricsRequest extends ClusterInfoMessage
@SerialVersionUID(1L)
case class ClusterMetricsRequest(initialDelay: String, interval: String) extends ClusterInfoMessage
@SerialVersionUID(1L)
case class ClusterMetricsStopRequest(handler: ActorRef) extends ClusterInfoMessage
@SerialVersionUID(1L)
case class ClusterMetricsResponse(nodeMetrics: Set[NodeMetrics]) extends ClusterInfoMessage
@SerialVersionUID(1L)
case object ClusterInfoRequest extends ClusterInfoMessage
@SerialVersionUID(1L)
case class ClusterInfoRequest(initialDelay: String, interval: String) extends ClusterInfoMessage
@SerialVersionUID(1L)
case class ClusterInfoStopRequest(handler: ActorRef) extends ClusterInfoMessage
@SerialVersionUID(1L)
case class ClusterInfoResponse(clusterState: CurrentClusterState) extends ClusterInfoMessage

@SerialVersionUID(1L)
case object Ping extends Message
@SerialVersionUID(1L)
case object Pong extends Message
