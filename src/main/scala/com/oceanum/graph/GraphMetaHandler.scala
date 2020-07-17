package com.oceanum.graph

import com.oceanum.cluster.exec.State

trait GraphMetaHandler {
  def onStart(richGraphMeta: RichGraphMeta): Unit

  def onRunning(richGraphMeta: RichGraphMeta, taskState: State): Unit

  def onComplete(richGraphMeta: RichGraphMeta): Unit

  def close(): Unit = {}
}

object GraphMetaHandler {
  def default(): GraphMetaHandler = new GraphMetaHandler {
    override def onRunning(richGraphMeta: RichGraphMeta, taskState: State): Unit = println("graphMeta: " + richGraphMeta)

    override def onComplete(richGraphMeta: RichGraphMeta): Unit = println("graphMeta complete: " + richGraphMeta)

    override def onStart(richGraphMeta: RichGraphMeta): Unit = println("graphMeta start: " + richGraphMeta)

    override def close(): Unit = println("handler closed")
  }
}