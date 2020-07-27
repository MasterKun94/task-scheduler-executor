package com.oceanum.graph

import com.oceanum.common.GraphMeta
import com.oceanum.exec.State

trait GraphMetaHandler {
  def onStart(richGraphMeta: GraphMeta): Unit

  def onRunning(richGraphMeta: GraphMeta, taskState: State): Unit

  def onComplete(richGraphMeta: GraphMeta): Unit

  def close(): Unit = {}
}

object GraphMetaHandler {
  def default(): GraphMetaHandler = new GraphMetaHandler {
    override def onRunning(richGraphMeta: GraphMeta, taskState: State): Unit = {
      println("state: " + taskState)
      println("graphMeta: " + richGraphMeta)
    }

    override def onComplete(richGraphMeta: GraphMeta): Unit = {
      println("graphMeta complete: " + richGraphMeta.graphStatus)
      richGraphMeta.tasks.foreach(println)
    }

    override def onStart(richGraphMeta: GraphMeta): Unit = {
      println("graphMeta start: " + richGraphMeta)
    }

    override def close(): Unit = println("handler closed")
  }
}