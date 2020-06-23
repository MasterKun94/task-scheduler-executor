package com.oceanum.api.impl

import akka.actor.{ActorPaths, ActorRef, ActorSystem, Props}
import akka.cluster.client.{ClusterClient, ClusterClientSettings}
import akka.pattern.ask
import akka.util.Timeout
import com.oceanum.ClusterStarter
import com.oceanum.api.{SchedulerClient, Task, TaskInstance}
import com.oceanum.cluster._
import com.oceanum.common.Environment
import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.JavaConversions.seqAsJavaList
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class SchedulerClientImpl(endpoint: ActorRef)(implicit executionContext: ExecutionContext, timeout: Timeout) extends SchedulerClient {
  override def execute(topic: String, task: Task, stateHandler: StateHandler = StateHandler.empty()): Future[TaskInstance] = {
    doExecute(AvailableExecutorRequest(topic), task, stateHandler)
  }

  override def executeAll(topic: String, task: Task, stateHandler: StateHandler = StateHandler.empty())(implicit timeWait: String): Future[TaskInstance] = {
    doExecute(AvailableExecutorsRequest(topic, timeWait), task, stateHandler)
  }

  private def getClient(executionContext: ExecutionContext): ActorRef = {
    SchedulerClientImpl.clientSystem.actorOf(Props(classOf[ExecutorFinder], endpoint, executionContext), "client-actor")
  }

  private def doExecute(requestMsg: Any, task: Task, stateHandler: StateHandler = StateHandler.empty()): Future[TaskInstance] = {
    val client = getClient(executionContext)
    client
      .ask(requestMsg)
      .mapTo[AvailableExecutorResponse]
      .flatMap(response => {
        val res = response.executor
          .map(executor => SchedulerClientImpl.clientSystem.actorOf(Props(classOf[ClientExecutor], executor.actor)))
          .map(client => client
            .ask(ExecuteOperatorRequest(task, stateHandler))
            .map(_ => client)
          )
          .toSeq
        Future.sequence(res).map(TaskInstance(_))
      })
  }
}

object SchedulerClientImpl {
  lazy val clientSystem: ActorSystem = ActorSystem(Environment.CLIENT_SYSTEM_NAME, loadConfig)

  def loadConfig: Config = {
    val configString =
      s"""
        |akka {
        | actor {
        |   provider = remote
        |   warn-about-java-serializer-usage = false
        |   serializers {
        |     java = "akka.serialization.JavaSerializer"
        |     proto = "akka.remote.serialization.ProtobufSerializer"
        |   }
        | }
        |  remote {
        |    enabled-transports = ["akka.remote.netty.tcp"]
        |    log-remote-lifecycle-events = off
        |    netty.tcp {
        |      hostname = "${Environment.CLIENT_HOST}"
        |      port = ${Environment.CLIENT_PORT}
        |      bind-hostname = "0.0.0.0"
        |      bind-port = ${Environment.CLIENT_PORT}
        |    }
        |  }
        |}
        |""".stripMargin
    ConfigFactory.parseString(configString)
  }

  def create(host: String, port: Int, seedNodes: String)(implicit executionContext: ExecutionContext, timeout: Timeout): SchedulerClient = {

    Environment.load(Environment.Key.CLIENT_HOST, host)
    Environment.load(Environment.Key.CLIENT_PORT, port.toString)

    val seeds = seedNodes
      .split(",")
      .map(_.trim)
      .filter(_.nonEmpty)
      .map(_.split(":"))
      .map(arr => if (arr.length == 1) arr :+ "3551" else arr)
      .map(_.mkString(":"))
    Environment.load(Environment.Key.CLUSTER_SEED_NODE, seeds.mkString(","))

    //先放一个contact-point, 系统会自动增加其它的点
    val initialContacts = Environment.CLUSTER_SEED_NODE
      .map(s => s + "/system/receptionist")
      .map(ActorPaths.fromString)
      .toSet

    val clusterClient = clientSystem.actorOf(
      ClusterClient.props(
        ClusterClientSettings(clientSystem)
          .withInitialContacts(initialContacts)),
      "client-endpoint")
    val actor = clientSystem.actorOf(Props(classOf[ClientListener], clusterClient), "client-event-listener")
    new SchedulerClientImpl(clusterClient)
  }
}
