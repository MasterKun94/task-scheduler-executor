package com.oceanum

import akka.Done
import akka.stream.scaladsl.{GraphDSL, RunnableGraph, SourceQueueWithComplete}
import com.oceanum.exec.{GraphMeta, RichGraphMeta, State}

import scala.concurrent.Future

package object graph {
  type Mat = (SourceQueueWithComplete[RichGraphMeta], Future[Done])
  type DslBuilder = GraphDSL.Builder[Mat]
  type Graph = RunnableGraph[Mat]

  case class OnStart(graphMeta: GraphMeta)
  case class  OnRunning(graphMeta: GraphMeta, taskState: State)
  case class  OnComplete(graphMeta: GraphMeta)
}
