package com.oceanum.actors

import akka.actor.{ActorSystem, Props}
import akka.cluster.client.ClusterClientReceptionist
import com.oceanum.ClusterStarter
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

  def start(topics: Seq[String], host: String, port: Int, seedNodes: Seq[String]): Unit = {

    val config = ConfigFactory
      .parseMap(Map(
        "akka.cluster.seed-nodes" -> seqAsJavaList(seedNodes),
        "akka.remote.netty.tcp.port" -> port,
        "akka.remote.netty.tcp.host" -> host
      ))
      .withFallback(ConfigFactory.load("application.conf"))
    println(config)
    val system = ActorSystem.create(Environment.CLUSTER_SYSTEM_NAME, config)
    system.actorOf(Props(classOf[ClusterNode]), "cluster-node")
    val service = system.actorOf(Props(classOf[ExecutionEndpoint], topics), "execution-endpoint")
    ClusterClientReceptionist(system).registerService(service)
    val receptionist = ClusterClientReceptionist(system).underlying
    system.actorOf(Props(classOf[ReceptionistListener], receptionist),"event-listener")
  }

  def main(args: Array[String]): Unit = {
    ClusterStarter.main(Array("--port=3551", "--topics=t1,a1"))
    ClusterStarter.main(Array("--port=3552", "--topics=t2,a2"))
    ClusterStarter.main(Array("--port=3553", "--topics=t3,a3"))
    //    Thread.sleep(3000)
    //    SchedulerClient.main(null)
  }
}
