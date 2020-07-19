package com.oceanum.graph

import akka.actor.Actor

import scala.collection.mutable

class GraphMetaHandlerActor(graphMetaHandler: GraphMetaHandler) extends Actor {
  val map: mutable.Map[Int, RichGraphMeta] = mutable.Map()

  override def postStop(): Unit = graphMetaHandler.close()

  override def receive: Receive = {
    case OnStart(meta) =>
      graphMetaHandler.onStart(meta)
      map += (meta.id -> meta)

    case OnRunning(meta, state) =>
      val oldMeta = map(meta.id)
      val newMeta = oldMeta merge meta
      graphMetaHandler.onRunning(newMeta, state)
      map += (meta.id -> newMeta)

    case OnComplete(meta) =>
      val oldMeta = map(meta.id)
      val newMeta = oldMeta merge meta
      graphMetaHandler.onComplete(newMeta)
      map -= meta.id

  }
}
