package com.oceanum.api.entities

import com.oceanum.annotation.ISerializationMessage

@ISerializationMessage("CLUSTER_NODE")
case class ClusterNode(host: String, status: String, roles: Seq[String])