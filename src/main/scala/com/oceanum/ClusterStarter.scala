package com.oceanum

import akka.actor.{ActorSystem, Props}
import akka.cluster.client.ClusterClientReceptionist
import com.oceanum.cluster.{ClusterNode, ExecutionEndpoint, ReceptionistListener}
import com.oceanum.common.Environment
import com.typesafe.config.ConfigFactory

import scala.collection.JavaConversions.{mapAsJavaMap, seqAsJavaList}
/**
 * @author chenmingkun
 * @date 2020/5/28
 */
object ClusterStarter {

  def main(args: Array[String]): Unit = {

    val arg: Map[String, String] = args.map(str => str.split("="))
      .map(arr => (arr(0), arr(1)))
      .toMap
    arg.foreach(println)
    val paths = arg.getOrElse("--prop", s"conf${Environment.PATH_SEPARATOR}application.properties,conf${Environment.PATH_SEPARATOR}application-env.properties")
      .split(",")
      .map(_.trim)
      .filter(_.nonEmpty)
    Environment.load(paths)
    val conf = Environment.parsePath(arg.getOrElse("--akka-conf", Environment.AKKA_CONF))
    val topics = arg
      .get("--topics")
      .map(_.split(",").map(_.trim))
      .getOrElse(Array.empty)
      .union(Environment.CLUSTER_NODE_TOPICS)
      .distinct
      .toSeq
    val host = arg.getOrElse("--host", Environment.CLUSTER_NODE_HOST)
    val port = arg.get("--port").map(_.toInt).getOrElse(Environment.CLUSTER_NODE_PORT)
    val seedNodes = arg.get("--seed-node")
      .map(_.split(",").map(_.trim).toSeq)
      .getOrElse(Environment.CLUSTER_SEED_NODE)

    start(topics, host, port, seedNodes, conf)
  }

  def start(topics: Seq[String], host: String, port: Int, seedNodes: Seq[String], conf: String): Unit = {

    val config = ConfigFactory
      .parseMap(Map(
        "akka.cluster.seed-nodes" -> seqAsJavaList(seedNodes),
        "akka.remote.netty.tcp.port" -> port,
        "akka.remote.netty.tcp.hostname" -> host
      ))
      .withFallback(ConfigFactory.parseString(
        s"""
          |akka {
          |    actor {
          |        provider = "akka.cluster.ClusterActorRefProvider"
          |    }
          |    remote {
          |        enabled-transports = ["akka.remote.netty.tcp"]
          |        log-remote-lifecycle-events = off
          |    }
          |    extensions = ["akka.cluster.client.ClusterClientReceptionist"]
          |}
          |""".stripMargin))
    println(conf)
    println(config)
    val system = ActorSystem.create(Environment.CLUSTER_SYSTEM_NAME, config)
    Environment.registrySystem(system)
    system.actorOf(Props(classOf[ClusterNode]), "cluster-node")
    val service = system.actorOf(Props(classOf[ExecutionEndpoint], topics), "execution-endpoint")
    ClusterClientReceptionist(system).registerService(service)
    val receptionist = ClusterClientReceptionist(system).underlying
    system.actorOf(Props(classOf[ReceptionistListener], receptionist),"event-listener")
  }
}
