package com.oceanum.pluggable

import akka.actor.{ActorRef, Props}
import com.oceanum.pluggable.javadsl.PluggableExecutor

import scala.collection.JavaConversions.mapAsScalaMap

abstract class ExecutionProxy(executor: PluggableExecutor) {
  private lazy val endpoint: ActorRef = system.actorOf(Props(classOf[PluggableSubEndPoint], primEndpoint, this))

  def updateInfo(javaInfo: javadsl.PluggableInfo): Unit = {
    val info = PluggableInfo(javaInfo.getMap.toMap)
    endpoint ! info
  }

  def updateState(javaState: javadsl.PluggableState): Unit = {
    val state = PluggableState(javaState.getState, javaState.getDesc.toMap)
    endpoint ! state
  }

  def kill(): Unit = {
    executor.kill()
  }

  def startRun(): Unit = {
  }
}