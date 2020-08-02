package com.oceanum.common

import akka.actor.{ActorRef, ActorSystem}
import com.typesafe.config.ConfigFactory

import scala.collection.JavaConversions.seqAsJavaList

/**
 * @author chenmingkun
 * @date 2020/8/2
 */
object ActorSystems {
  lazy val SYSTEM: ActorSystem = if (Environment.MODE equals "cluster") clusterSystem() else clientSystem()

  private def clusterSystem(): ActorSystem = {
    import scala.collection.JavaConversions.mapAsJavaMap
    val config = ConfigFactory
      .parseMap(Map(
        "akka.cluster.seed-nodes" -> seqAsJavaList(Environment.CLUSTER_NODE_SEEDS),
        "akka.remote.netty.tcp.hostname" -> Environment.HOST,
        "akka.remote.netty.tcp.port" -> Environment.CLUSTER_NODE_PORT,
        "akka.remote.netty.tcp.bind-hostname" -> Environment.HOST,
        "akka.remote.netty.tcp.bind-port" -> Environment.CLUSTER_NODE_PORT,
        "akka.cluster.metrics.collector.sample-interval" -> Environment.CLUSTER_NODE_METRICS_SAMPLE_INTERVAL,
        "akka.loggers" -> seqAsJavaList(Seq(Environment.CLUSTER_NODE_LOGGER))
      ))
      .withFallback(ConfigFactory.parseString(
        """
          |akka {
          | actor {
          |   provider = "akka.cluster.ClusterActorRefProvider"
          |   warn-about-java-serializer-usage = false
          |  }
          | remote {
          |  enabled-transports = ["akka.remote.netty.tcp"]
          |  log-remote-lifecycle-events = off
          | }
          | extensions = ["akka.cluster.client.ClusterClientReceptionist", "akka.cluster.metrics.ClusterMetricsExtension"]
          |}
          |""".stripMargin))
    ActorSystem.create(Environment.CLUSTER_NODE_SYSTEM_NAME, config)
  }

  private def clientSystem(): ActorSystem = {
    val configString =
      s"""
         |akka {
         | actor {
         |   provider = remote
         |   warn-about-java-serializer-usage = false
         |
         | }
         | loggers = ["${Environment.CLIENT_NODE_LOGGER}"]
         | remote {
         |    enabled-transports = ["akka.remote.netty.tcp"]
         |    log-remote-lifecycle-events = off
         |    netty.tcp {
         |      hostname = "${Environment.HOST}"
         |      port = ${Environment.CLIENT_NODE_PORT}
         |      bind-hostname = "${Environment.HOST}"
         |      bind-port = ${Environment.CLIENT_NODE_PORT}
         |    }
         |  }
         |}
         |""".stripMargin
    ConfigFactory.parseString(configString)
    ActorSystem(Environment.CLIENT_NODE_SYSTEM_NAME, ConfigFactory.parseString(configString))
  }
}
