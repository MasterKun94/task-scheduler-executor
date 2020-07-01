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
case class AvailableExecutor(actor: ActorRef, taskInfo: NodeTaskInfoResponse, topics: Seq[String]) extends Message
@SerialVersionUID(1L)
case class ExecuteOperatorRequest(operatorMessage: Task, stateHandler: StateHandler) extends Message
@SerialVersionUID(1L)
case class ExecuteOperatorResponse(operatorMessage: Task, stateHandler: StateHandler) extends Message
@SerialVersionUID(1L)
case class HandleState(handler: StateHandler) extends Message
@SerialVersionUID(1L)
case class HandleOnComplete(handler: StateHandler) extends Message

trait ClusterMessage extends Message
@SerialVersionUID(1L)
case class ClusterInfoMessageHolder(message: ClusterMessage, actorRef: ActorRef) extends Message
@SerialVersionUID(1L)
case object ClusterMetricsRequest extends ClusterMessage
@SerialVersionUID(1L)
case class ClusterMetricsRequest(initialDelay: String, interval: String) extends ClusterMessage
@SerialVersionUID(1L)
case class ClusterMetricsStopRequest(handler: ActorRef) extends ClusterMessage
@SerialVersionUID(1L)
case class ClusterMetricsResponse(nodeMetrics: Set[NodeMetrics]) extends ClusterMessage
@SerialVersionUID(1L)
case object ClusterStateRequest extends ClusterMessage
@SerialVersionUID(1L)
case class ClusterStateRequest(initialDelay: String, interval: String) extends ClusterMessage
@SerialVersionUID(1L)
case class ClusterStateStopRequest(handler: ActorRef) extends ClusterMessage
@SerialVersionUID(1L)
case class ClusterStateResponse(clusterState: CurrentClusterState) extends ClusterMessage
@SerialVersionUID(1L)
case object NodeTaskInfoRequest extends ClusterMessage
@SerialVersionUID(1L)
case class NodeTaskInfoRequest(initialDelay: String, interval: String) extends ClusterMessage
@SerialVersionUID(1L)
case class NodeTaskInfoStopRequest(handler: ActorRef) extends ClusterMessage
@SerialVersionUID(1L)
case class NodeTaskInfoResponse(preparing: Int, running: Int, success: Int, failed: Int, retry: Int, killed: Int, complete: Int) extends ClusterMessage

@SerialVersionUID(1L)
case class NodeInfo(hostname: String, ip: String, topics: Seq[String]) extends ClusterMessage
@SerialVersionUID(1L)
case class ClusterInfo(nodeInfos: Set[NodeInfo]) extends ClusterMessage
@SerialVersionUID(1L)
case class ClusterInfoRequest()

@SerialVersionUID(1L)
case object Ping extends Message
@SerialVersionUID(1L)
case object Pong extends Message
