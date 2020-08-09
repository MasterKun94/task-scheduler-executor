package com.oceanum.api.entities

import com.oceanum.annotation.ISerializationMessage
import com.oceanum.common.CoordStatus

@ISerializationMessage("COORDINATOR_STATUS")
case class CoordinatorStatus(name: String, status: CoordStatus)
