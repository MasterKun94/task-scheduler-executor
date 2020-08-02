package com.oceanum.graph

import com.oceanum.common.{GraphMeta, TaskMeta}
import com.oceanum.exec.State
import com.oceanum.persistence.{Catalog, MetaIdFactory, Repository}

import scala.concurrent.Future
import scala.util.{Failure, Success}

trait GraphMetaHandler {
  def onStart(graphMeta: GraphMeta): Unit

  def onRunning(graphMeta: GraphMeta, taskState: State): Unit

  def onComplete(graphMeta: GraphMeta): Unit

  def close(): Unit = {}
}

object GraphMetaHandler {
  lazy val empty: GraphMetaHandler = new GraphMetaHandler {
    override def onStart(graphMeta: GraphMeta): Unit = { }
    override def onRunning(graphMeta: GraphMeta, taskState: State): Unit = { }
    override def onComplete(graphMeta: GraphMeta): Unit = { }
  }

  lazy val default: GraphMetaHandler = new GraphMetaHandler {
    private val graphRepo = Catalog.getRepository[GraphMeta]
    import scala.concurrent.ExecutionContext.Implicits.global

    override def onRunning(graphMeta: GraphMeta, taskState: State): Unit = save(graphMeta)

    override def onComplete(graphMeta: GraphMeta): Unit = save(graphMeta)

    override def onStart(graphMeta: GraphMeta): Unit = save(graphMeta)

    override def close(): Unit = {}

    private def save(graphMeta: GraphMeta): Unit = {
      graphRepo.save(MetaIdFactory.getGraphMetaId(graphMeta), graphMeta)
        .onComplete {
          case Success(value) =>
          case Failure(exception) => exception.printStackTrace()
        }
    }
  }
}