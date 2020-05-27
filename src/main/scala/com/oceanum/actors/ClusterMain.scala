package com.oceanum.actors

import akka.actor.{ActorSystem, Props}
import akka.cluster.client.ClusterClientReceptionist
import com.oceanum.common.Environment
import com.typesafe.config.ConfigFactory

import scala.collection.JavaConversions.seqAsJavaList
import scala.concurrent.ExecutionContext
import scala.collection.JavaConversions.mapAsJavaMap

/**
 * @author chenmingkun
 * @date 2020/5/3
 */
object ClusterMain {
  def start(port: Int): Unit = {
    val config = ConfigFactory
      .parseMap(Map(
        "akka.cluster.seed-nodes" -> seqAsJavaList(Environment.CLUSTER_SEED_NODE),
        "akka.remote.netty.tcp.port" -> port
      ))
      .withFallback(ConfigFactory.load("application.conf"))
    println(config)
    val system = ActorSystem.create(Environment.CLUSTER_SYSTEM_NAME, config)
    system.actorOf(Props(classOf[ClusterNode]), "cluster-node")
    val service = system.actorOf(Props(classOf[ExecutionEndpoint], ExecutionContext.global), "execution-endpoint")
    ClusterClientReceptionist(system).registerService(service)
    val receptionist = ClusterClientReceptionist(system).underlying
    system.actorOf(Props(classOf[ReceptionistListener], receptionist),"event-listener")
  }

  def main(args: Array[String]): Unit = {
    if (Environment.DEV_MODE) {
      start(3551)
      start(3552)
      start(3553)
    } else {
      start(Environment.CLUSTER_NODE_PORT)
    }
//    Thread.sleep(3000)
//    SchedulerClient.main(null)
  }
}
