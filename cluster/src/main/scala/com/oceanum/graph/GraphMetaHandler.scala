package com.oceanum.graph

import com.oceanum.common.GraphMeta
import com.oceanum.exec.State
import com.oceanum.persistence.PersistenceUtil

trait GraphMetaHandler {
  def onStart(graphMeta: GraphMeta): Unit

  def onRunning(graphMeta: GraphMeta, taskState: State): Unit

  def onComplete(graphMeta: GraphMeta): Unit

  def close(): Unit = {}
}

object GraphMetaHandler {
  def default(): GraphMetaHandler = new GraphMetaHandler {
    override def onRunning(graphMeta: GraphMeta, taskState: State): Unit = {
      PersistenceUtil.save(graphMeta)
      println("state: " + taskState)
      println("graphMeta: " + graphMeta)
    }

    override def onComplete(graphMeta: GraphMeta): Unit = {
      PersistenceUtil.save(graphMeta)
      println("graphMeta complete: " + graphMeta.graphStatus)
      graphMeta.tasks.foreach(println)
    }

    override def onStart(graphMeta: GraphMeta): Unit = {
      PersistenceUtil.save(graphMeta)
      println("graphMeta start: " + graphMeta)
    }

    override def close(): Unit = println("handler closed")
  }
}