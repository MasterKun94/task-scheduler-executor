package com.oceanum

import akka.Done
import akka.stream.scaladsl.{GraphDSL, RunnableGraph, SourceQueueWithComplete}
import com.oceanum.common.{GraphMeta, RichGraphMeta}
import com.oceanum.exec.State
import com.oceanum.graph.Operators.Operator
import com.oceanum.graph.StreamFlows.StreamFlow

import scala.concurrent.Future

package object graph {
  type Mat = (SourceQueueWithComplete[RichGraphMeta], Future[Done])
  type DslBuilder = GraphDSL.Builder[Mat]
  type Graph = RunnableGraph[Mat]
  type BaseOperator = Operator[_<: StreamFlow]

  case class OnStart(graphMeta: GraphMeta)
  case class OnRunning(graphMeta: GraphMeta, taskState: State)
  case class OnComplete(graphMeta: GraphMeta)

  case class GraphDefine(nodes: Map[String, Operator[_<:StreamFlow]], edges: Map[String, Array[String]], env: Map[String, Any])
}
