package com.oceanum.api.entities

import com.oceanum.annotation.ISerializationMessage
import com.oceanum.common.NodeStatus

@ISerializationMessage("CLUSTER_NODE")
case class ClusterNode(host: String, status: NodeStatus, roles: Seq[String])