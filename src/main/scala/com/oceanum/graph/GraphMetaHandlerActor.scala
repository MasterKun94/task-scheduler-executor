package com.oceanum.graph

import akka.actor.Actor

class GraphMetaHandlerActor(graphMetaHandler: GraphMetaHandler) extends Actor {

  override def postStop(): Unit = graphMetaHandler.close()

  override def receive: Receive = {
    case OnStart(meta) =>
      graphMetaHandler.onStart(meta)
      context.become(running(meta))
  }

  def running(oldMeta: RichGraphMeta): Receive = {
    case OnRunning(meta, state) =>
      val newMeta = oldMeta merge meta
      graphMetaHandler.onRunning(newMeta, state)
      context.become(running(newMeta))
    case OnComplete(meta) =>
      val newMeta = oldMeta merge meta
      graphMetaHandler.onComplete(newMeta)
      context.become(running(newMeta))
  }
}
