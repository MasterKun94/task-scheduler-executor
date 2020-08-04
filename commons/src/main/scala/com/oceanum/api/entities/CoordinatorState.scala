package com.oceanum.api.entities

case class CoordinatorState(name: String, status: CoordinatorState.value)

object CoordinatorState extends Enumeration {
  type value = Value
  val RUNNING, SUSPENDED, STOPPED = Value
}
