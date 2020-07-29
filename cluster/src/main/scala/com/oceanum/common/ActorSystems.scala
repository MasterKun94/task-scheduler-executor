package com.oceanum.common

import akka.actor.{ActorRef, ActorSystem}
import akka.cluster.pubsub.DistributedPubSub
import com.typesafe.config.ConfigFactory

import scala.collection.JavaConversions.seqAsJavaList

object ActorSystems {
  lazy val CLUSTER_SYSTEM: ActorSystem = clusterSystem()
  lazy val CLIENT_SYSTEM: ActorSystem = clientSystem()
  lazy val FILE_SERVER_SYSTEM: ActorSystem = fileServerSystem()
  lazy val CLUSTER_NODE_MEDIATOR: ActorRef = DistributedPubSub(CLUSTER_SYSTEM).mediator


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

  private def fileServerSystem(): ActorSystem = {
    val configString =
      s"""
         |file-io-dispatcher {
         |  type = Dispatcher
         |  executor = "thread-pool-executor"
         |  thread-pool-executor {
         |    core-pool-size-min = ${Environment.FILE_SERVER_DISPATCHER_CORE_POOL_SIZE_MIN}
         |    core-pool-size-factor = ${Environment.FILE_SERVER_DISPATCHER_CORE_POOL_SIZE_FACTOR}
         |    core-pool-size-max = ${Environment.FILE_SERVER_DISPATCHER_CORE_POOL_SIZE_MAX}
         |  }
         |  throughput = 1000
         |}
         |akka.loggers = ["${Environment.FILE_SERVER_LOGGER}"]
         |
         |akka.http {
         |  host-connection-pool {
         |    max-connections = ${Environment.FILE_SERVER_HOST_CONNECTION_POOL_MAX_CONNECTIONS}
         |    min-connections = ${Environment.FILE_SERVER_HOST_CONNECTION_POOL_MIN_CONNECTIONS}
         |    min-retries = ${Environment.FILE_SERVER_HOST_CONNECTION_POOL_MAX_RETRIES}
         |    max-open-requests = ${Environment.FILE_SERVER_HOST_CONNECTION_POOL_MAX_OPEN_REQUESTS}
         |  }
         |}
         |""".stripMargin
    ConfigFactory.parseString(configString)
    ActorSystem(Environment.FILE_SERVER_SYSTEM_NAME, ConfigFactory.parseString(configString))
  }
}
