package com.oceanum.api.entities

import akka.routing.ConsistentHash
import com.oceanum.annotation.ISerializationMessage

import scala.util.Random

@ISerializationMessage("CLUSTER_NODES")
case class ClusterNodes(nodes: Seq[ClusterNode]) {
  def randomSelect(): ClusterNode = Random.shuffle(nodes).head

  def consistentHashSelect(key: String): ClusterNode = ConsistentHash(nodes, 5).nodeFor(key)
}
