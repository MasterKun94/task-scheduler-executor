package com.oceanum

import akka.Done
import akka.stream.scaladsl.{GraphDSL, RunnableGraph, SourceQueueWithComplete}
import com.oceanum.annotation.ISerializationMessage
import com.oceanum.client.SingleTaskInstanceRef
import com.oceanum.common.{GraphMeta, RichGraphMeta}
import com.oceanum.exec.State
import com.oceanum.graph.Operators.Operator
import com.oceanum.graph.StreamFlows.StreamFlow

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future

package object graph {
  type Mat = (SourceQueueWithComplete[Message], Future[Done])
  type DslBuilder = GraphDSL.Builder[Mat]
  type Graph = RunnableGraph[Mat]
  type BaseOperator = Operator[_<: StreamFlow]

  case class OnStart(graphMeta: GraphMeta)
  case class OnRunning(graphMeta: GraphMeta, taskState: State)
  case class OnComplete(graphMeta: GraphMeta)

  @ISerializationMessage("GRAPH_DEFINE")
  case class GraphDefine(nodes: Map[String, Operator[_<:StreamFlow]], edges: Map[String, Array[String]], env: Map[String, Any])

  case class Message(meta: RichGraphMeta, instances: TrieMap[SingleTaskInstanceRef, Unit])

  case class GraphHook(instances: TrieMap[SingleTaskInstanceRef, Unit]) {
    def kill(): Future[Unit] = {
      import com.oceanum.common.Environment.NONE_BLOCKING_EXECUTION_CONTEXT
      Future.sequence(instances.keys.toSeq.map(_.kill())).map(_ => Unit)
    }
  }
}
