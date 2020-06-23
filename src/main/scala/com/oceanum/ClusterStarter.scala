package com.oceanum

import java.io.File

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
    parseParam(args)
    start()
  }

  def parseParam(args: Array[String]): Unit = {
    val arg: Map[String, String] = args.map(str => str.split("="))
      .map(arr => (arr(0), arr(1)))
      .toMap
    println("args: ")
    arg.foreach(kv => println("\t" + kv))

    Environment.load(Environment.Key.BASE_PATH, arg.getOrElse("--base-path", new File(".").getCanonicalPath))

    val paths = arg.getOrElse("--conf", s"conf${Environment.PATH_SEPARATOR}application.properties,conf${Environment.PATH_SEPARATOR}application-env.properties")
      .split(",")
      .map(_.trim)
      .filter(_.nonEmpty)
    Environment.load(paths)

    val topics = arg
      .get("--topics")
      .map(str2arr)
      .getOrElse(Array.empty)
      .union(str2arr(Environment.getProperty(Environment.Key.CLUSTER_NODE_TOPICS)))
      .distinct
      .toSeq

    Environment.load(Environment.Key.CLUSTER_NODE_TOPICS, topics.mkString(","))

    val host = arg.getOrElse("--host", Environment.getProperty(Environment.Key.CLUSTER_NODE_HOST))
    Environment.load(Environment.Key.CLUSTER_NODE_HOST, host)

    val port = arg.getOrElse("--port", Environment.getProperty(Environment.Key.CLUSTER_NODE_PORT)).toInt
    Environment.load(Environment.Key.CLUSTER_NODE_PORT, port.toString)

    val seedNodes: Seq[String] = str2arr(arg
      .getOrElse("--seed-node", Environment.getProperty(Environment.Key.CLUSTER_SEED_NODE)))
      .map(_.split(":"))
      .map(arr => if (arr.length == 1) arr :+ "3551" else arr)
      .map(_.mkString(":"))
      .toSeq

    Environment.load(Environment.Key.CLUSTER_SEED_NODE, seedNodes.mkString(","))
    Environment.print()
  }

  def start(): Unit = {

    val config = ConfigFactory
      .parseMap(Map(
        "akka.cluster.seed-nodes" -> seqAsJavaList(Environment.CLUSTER_SEED_NODE),
        "akka.remote.netty.tcp.hostname" -> Environment.CLUSTER_NODE_HOST,
        "akka.remote.netty.tcp.port" -> Environment.CLUSTER_NODE_PORT,
        "akka.remote.netty.tcp.bind-hostname" -> "0.0.0.0",
        "akka.remote.netty.tcp.bind-port" -> Environment.CLUSTER_NODE_PORT
      ))
      .withFallback(ConfigFactory.parseString(
        s"""
           |akka {
           | actor {
           |   provider = "akka.cluster.ClusterActorRefProvider"
           |   warn-about-java-serializer-usage = false
           |  }
           | remote {
           |  enabled-transports = ["akka.remote.netty.tcp"]
           |  log-remote-lifecycle-events = off
           | }
           | extensions = ["akka.cluster.client.ClusterClientReceptionist"]
           |}
           |""".stripMargin))
    val system = ActorSystem.create(Environment.CLUSTER_SYSTEM_NAME, config)
    Environment.registrySystem(system)
    system.actorOf(Props(classOf[ClusterNode]), "cluster-node")
    val service = system.actorOf(Props(classOf[ExecutionEndpoint], Environment.CLUSTER_NODE_TOPICS), "execution-endpoint")
    ClusterClientReceptionist(system).registerService(service)
    val receptionist = ClusterClientReceptionist(system).underlying
    system.actorOf(Props(classOf[ReceptionistListener], receptionist),"event-listener")
  }

  private def str2arr(string: String): Array[String] = {
    string.split(",")
      .map(_.trim)
      .filter(_.nonEmpty)
  }
}
