package com.oceanum.api.entities

import com.oceanum.annotation.ISerializationMessage

@ISerializationMessage("COORDINATOR_STATUS")
case class CoordinatorStatus(name: String, status: CoordinatorStatus.value)

object CoordinatorStatus extends Enumeration {
  type value = Value
  val RUNNING, SUSPENDED, STOPPED = Value
}
