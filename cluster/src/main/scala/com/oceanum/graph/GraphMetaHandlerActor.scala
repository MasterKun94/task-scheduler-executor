package com.oceanum.graph

import akka.actor.Actor

import scala.collection.mutable

class GraphMetaHandlerActor(graphMetaHandler: GraphMetaHandler) extends Actor {
  val map: mutable.Map[Int, RichGraphMeta] = mutable.Map()

  override def postStop(): Unit = graphMetaHandler.close()

  override def receive: Receive = {
    case OnStart(meta) =>
//      println("on start" + map + ": " + meta)
      graphMetaHandler.onStart(meta)
      map += (meta.id -> meta.asInstanceOf[RichGraphMeta])

    case OnRunning(meta, state) =>
//      println("on running" + map + ": " + meta)
      val oldMeta = map(meta.id)
      val newMeta = oldMeta merge meta
      graphMetaHandler.onRunning(newMeta, state)
      map += (meta.id -> newMeta)

    case OnComplete(meta) =>
//      println("on complete" + map + ": " + meta)
      val oldMeta = map(meta.id)
      val newMeta = oldMeta merge meta
      graphMetaHandler.onComplete(newMeta)
      map -= meta.id

  }
}
