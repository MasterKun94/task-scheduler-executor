package com.oceanum.graph

import akka.actor.Actor
import com.oceanum.common.RichGraphMeta

import scala.collection.mutable

class GraphMetaHandlerActor(graphMetaHandlers: Array[GraphMetaHandler]) extends Actor {
  val map: mutable.Map[Int, RichGraphMeta] = mutable.Map()

  override def postStop(): Unit = {
    for (graphMetaHandler <- graphMetaHandlers) {
      graphMetaHandler.close()
    }
  }

  override def receive: Receive = {
    case OnStart(meta) =>
//      println("on start" + map + ": " + meta)
      for (graphMetaHandler <- graphMetaHandlers) {
        graphMetaHandler.onStart(meta)
      }

      map += (meta.id -> RichGraphMeta(meta))

    case OnRunning(meta, state) =>
//      println("on running" + map + ": " + meta)
      val oldMeta = map(meta.id)
      val newMeta = oldMeta merge meta
      for (graphMetaHandler <- graphMetaHandlers) {
        graphMetaHandler.onRunning(meta, state)
      }
      map += (meta.id -> newMeta)

    case OnComplete(meta) =>
//      println("on complete" + map + ": " + meta)
      val oldMeta = map(meta.id)
      val newMeta = oldMeta merge meta
    for (graphMetaHandler <- graphMetaHandlers) {
      graphMetaHandler.onComplete(meta)
    }
      map -= meta.id

  }
}
