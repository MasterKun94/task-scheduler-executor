package com.oceanum

import akka.actor.Props
import akka.cluster.client.ClusterClientReceptionist
import com.oceanum.cluster.{ClusterNode, ExecutionEndpoint, ReceptionistListener}
import com.oceanum.common.Environment
import com.oceanum.file.ClusterFileServer
import com.oceanum.metrics.MetricsListener
import com.oceanum.serialize.JsonSerialization
/**
 * @author chenmingkun
 * @date 2020/5/28
 */
object ClusterStarter {
  def main(args: Array[String]): Unit = {
    Environment.loadArgs(args)
    Environment.print()
    start()
  }

  def start(): Unit = {
    val system = Environment.CLUSTER_NODE_SYSTEM
    system.actorOf(Props(classOf[ClusterNode]), "cluster-node")
    val service = system.actorOf(Props(classOf[ExecutionEndpoint]), "execution-endpoint")
    ClusterClientReceptionist(system).registerService(service)
    val receptionist = ClusterClientReceptionist(system).underlying
    system.actorOf(Props(classOf[ReceptionistListener], receptionist),"event-listener")
    MetricsListener.start()
    ClusterFileServer.start()
    JsonSerialization.init()
  }
}
