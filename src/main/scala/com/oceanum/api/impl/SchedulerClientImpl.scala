package com.oceanum.api.impl

import akka.actor.{ActorPaths, ActorRef, ActorSystem, Props}
import akka.cluster.client.{ClusterClient, ClusterClientSettings}
import akka.pattern.ask
import akka.util.Timeout
import com.oceanum.ClusterStarter
import com.oceanum.api.{SchedulerClient, Task, TaskInstance}
import com.oceanum.cluster._
import com.oceanum.common.Environment
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class SchedulerClientImpl(endpoint: ActorRef)(implicit executionContext: ExecutionContext, timeout: Timeout) extends SchedulerClient {

  override def execute(topic: String, task: Task, stateHandler: StateHandler = StateHandler.empty()): Future[TaskInstance] = {
    doExecute(AvailableExecutorRequest(topic), task, stateHandler)
  }

  override def executeAll(topic: String, task: Task, stateHandler: StateHandler = StateHandler.empty())(implicit timeWait: FiniteDuration): Future[TaskInstance] = {
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
            .ask(ExecuteOperatorRequest(task, CheckStateScheduled(stateHandler.checkInterval(), stateHandler)))
            .map(_ => client)
          )
          .toSeq
        Future.sequence(res).map(TaskInstance(_))
      })
  }
}

object SchedulerClientImpl {
  val clientSystem: ActorSystem = ActorSystem(Environment.CLIENT_SYSTEM_NAME, ConfigFactory.load("client.conf"))

  def create(implicit executionContext: ExecutionContext, timeout: Timeout): SchedulerClient = {
    //先放一个contact-point, 系统会自动增加其它的点
    val initialContacts = Environment.CLUSTER_SEED_NODE.map(s => s + "/system/receptionist").map(ActorPaths.fromString).toSet

    val clusterClient = clientSystem.actorOf(
      ClusterClient.props(
        ClusterClientSettings(clientSystem)
          .withInitialContacts(initialContacts)),
      "client-endpoint")
    val actor = clientSystem.actorOf(Props(classOf[ClientListener], clusterClient), "client-event-listener")
    new SchedulerClientImpl(clusterClient)
  }
}
