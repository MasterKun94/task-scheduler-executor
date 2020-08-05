package com.oceanum.api.entities

import com.oceanum.annotation.ISerializationMessage

@ISerializationMessage("CLUSTER_NODES")
case class ClusterNodes(nodes: Seq[ClusterNode]) {

}
