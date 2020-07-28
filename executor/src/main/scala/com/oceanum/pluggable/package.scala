package com.oceanum

import akka.actor.{ActorSelection, ActorSystem}

package object pluggable {
  case class PluggableInfo(map: Map[String, Any])
  case class PluggableState(state: String, desc: Map[String, Any])

  trait TaskComplete
  case class TaskSuccess(finalState: Option[PluggableState] = None) extends TaskComplete
  case class TaskFailed(e: Throwable, finalState: Option[PluggableState] = None) extends TaskComplete
  case class TaskKilled(finalState: Option[PluggableState] = None) extends TaskComplete

  case object CheckPluggableState
  case object Kill

  val system: ActorSystem = ???
  val primEndpoint: ActorSelection = ???
}
