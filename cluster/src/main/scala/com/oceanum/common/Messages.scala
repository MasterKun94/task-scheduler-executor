package com.oceanum.common

import akka.actor.ActorRef
import akka.cluster.ClusterEvent.CurrentClusterState
import akka.cluster.metrics.NodeMetrics
import com.oceanum.api.entities.NodeTaskInfo
import com.oceanum.client.{StateHandler, Task}

@SerialVersionUID(1L)
trait Message {}
case class PrepareMessage(message: RichTaskMeta => RichTaskMeta) extends Message
case class RunningMessage(message: RichTaskMeta => RichTaskMeta) extends Message
case class FailedMessage(message: RichTaskMeta => RichTaskMeta) extends Message
case class SuccessMessage(message: RichTaskMeta => RichTaskMeta) extends Message
case class RetryMessage(message: RichTaskMeta => RichTaskMeta) extends Message
case object KillMessage extends Message
case class TimeoutMessage(message: RichTaskMeta => RichTaskMeta) extends Message
case class StartMessage(message: RichTaskMeta => RichTaskMeta) extends Message

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
case class AvailableExecutorResponse(executor: Seq[AvailableExecutor]) extends Message
@SerialVersionUID(1L)
case class AvailableExecutor(actor: ActorRef, taskInfo: NodeTaskInfo, topics: Seq[String]) extends Message
@SerialVersionUID(1L)
case class ExecuteOperatorRequest(task: Task) extends Message
@SerialVersionUID(1L)
case class ExecuteOperatorResponse(metadata: RichTaskMeta) extends Message
@SerialVersionUID(1L)
case class HandleState(handler: StateHandler) extends Message
@SerialVersionUID(1L)
case class HandleOnComplete(handler: StateHandler) extends Message

@SerialVersionUID(1L)
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
case class ClusterMetrics(nodeMetrics: Set[NodeMetrics]) extends ClusterMessage
@SerialVersionUID(1L)
case object ClusterStateRequest extends ClusterMessage
@SerialVersionUID(1L)
case class ClusterStateRequest(initialDelay: String, interval: String) extends ClusterMessage
@SerialVersionUID(1L)
case class ClusterStateStopRequest(handler: ActorRef) extends ClusterMessage {
  handler.path
}
@SerialVersionUID(1L)
case class ClusterState(clusterState: CurrentClusterState) extends ClusterMessage
@SerialVersionUID(1L)
case object NodeTaskInfoRequest extends ClusterMessage
@SerialVersionUID(1L)
case class NodeTaskInfoRequest(initialDelay: String, interval: String) extends ClusterMessage
@SerialVersionUID(1L)
case class NodeTaskInfoStopRequest(handler: ActorRef) extends ClusterMessage
@SerialVersionUID(1L)
case class StopRequest(handler: ActorRef) extends ClusterMessage

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
