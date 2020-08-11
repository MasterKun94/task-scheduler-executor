package com.oceanum.pluggable

import akka.actor.Actor
import com.oceanum.pluggable.Env.UpdateState

class PluggablePrimEndpoint(primListener: PrimListener) extends Actor {

  override def receive: Receive = {
    case UpdateState(info) =>
      context.become(onRunning(info))
      primListener.updateState(info)
  }

  def onRunning(preInfo: Map[String, String]): Receive = {
    case UpdateState(info) =>
      val newInfo = preInfo ++ info
      primListener.updateState(newInfo)
      context.become(onRunning(newInfo))
  }
}
