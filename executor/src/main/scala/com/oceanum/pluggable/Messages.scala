package com.oceanum.pluggable

case class PluggableInfo(map: Map[String, AnyRef])
case class PluggableState(state: String, desc: Map[String, AnyRef])

trait TaskComplete
case class TaskSuccess(finalState: Option[PluggableState] = None) extends TaskComplete
case class TaskFailed(e: Throwable, finalState: Option[PluggableState] = None) extends TaskComplete
case class TaskKilled(finalState: Option[PluggableState] = None) extends TaskComplete

case object CheckPluggableState
case object Kill
