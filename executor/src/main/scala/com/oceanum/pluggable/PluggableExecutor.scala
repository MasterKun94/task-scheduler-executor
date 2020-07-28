package com.oceanum.pluggable

import akka.actor.{ActorRef, Props}

abstract class PluggableExecutor {
  private lazy val endpoint: ActorRef = system.actorOf(Props(classOf[PluggableSubEndPoint], primEndpoint, this))

  def updateInfo(info: PluggableInfo): Unit = endpoint ! info

  def updateState(state: PluggableState): Unit = endpoint ! state

  def startRun(): TaskComplete

  def kill(): Unit

  def close(): Unit = {
    system.terminate()
    System.exit(0)
  }
}
