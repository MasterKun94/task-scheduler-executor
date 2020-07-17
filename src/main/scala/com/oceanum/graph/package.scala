package com.oceanum

import akka.Done
import akka.stream.scaladsl.{GraphDSL, RunnableGraph, SourceQueueWithComplete}
import com.oceanum.cluster.exec.State

import scala.concurrent.Future

package object graph {
  type Mat = (SourceQueueWithComplete[RichGraphMeta], Future[Done])
  type DslBuilder = GraphDSL.Builder[Mat]
  type Graph = RunnableGraph[Mat]

  case class OnStart(graphMeta: RichGraphMeta)
  case class  OnRunning(graphMeta: RichGraphMeta, taskState: State)
  case class  OnComplete(graphMeta: RichGraphMeta)
}
