package com.oceanum

import akka.actor.Props
import akka.cluster.client.ClusterClientReceptionist
import com.oceanum.api.HttpServer
import com.oceanum.cluster.{ClusterNode, ExecutionEndpoint, ReceptionistListener}
import com.oceanum.common.{ActorSystems, Environment}
import com.oceanum.singleton.{FallbackListener, MetricsListener}

/**
 * @author chenmingkun
 * @date 2020/5/28
 */
object ClusterStarter {
  def main(args: Array[String]): Unit = {
    Environment.loadEnv("--mode=cluster" +: args)
    Environment.printEnv()
    Environment.initSystem()
    start()
  }

  def start(): Unit = {
    val system = ActorSystems.SYSTEM
    system.deadLetters
    system.actorOf(Props(classOf[ClusterNode]), "cluster-node")
    val service = system.actorOf(Props(classOf[ExecutionEndpoint]), "execution-endpoint")
    ClusterClientReceptionist(system).registerService(service)
    val receptionist = ClusterClientReceptionist(system).underlying
    system.actorOf(Props(classOf[ReceptionistListener], receptionist),"event-listener")
    MetricsListener.start(system)
    FallbackListener.start(system)
    HttpServer.start()
  }
}
