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

  object ReRunStrategy extends Enumeration {
    type value = Value
    val NONE, RUN_ALL, RUN_ONLY_FAILED, RUN_ALL_AFTER_FAILED = Value
  }

  object FallbackStrategy extends Enumeration {
    type value = Value
    val CONTINUE, SHUTDOWN = Value
  }

  object GraphStatus extends Enumeration {
    type value = Value
    val OFFLINE, RUNNING, SUCCESS, EXCEPTION, FAILED, KILLED = Value
  }

  case class GraphDefine(nodes: Map[String, Operator[_<:StreamFlow]], edges: Map[String, Array[String]], env: Map[String, Any])
}
