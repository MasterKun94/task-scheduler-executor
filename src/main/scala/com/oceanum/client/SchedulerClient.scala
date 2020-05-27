package com.oceanum.client

import akka.actor.{ActorPaths, ActorRef, ActorSystem, Props}
import akka.cluster.client.{ClusterClient, ClusterClientSettings}
import akka.pattern.ask
import akka.util.Timeout
import com.oceanum.actors.{ClientActor, ClientExecutor, ClientListener, StateHandler}
import com.oceanum.common.Environment
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
 * @author chenmingkun
 * @date 2020/5/4
 */
class SchedulerClient(endpoint: ActorRef)(implicit executionContext: ExecutionContext, timeout: Timeout) {
  private def getClient(executionContext: ExecutionContext): ActorRef = {
    SchedulerClient.clientSystem.actorOf(Props(classOf[ClientActor], endpoint, executionContext), "client-actor")
  }

  def execute(topic: String, operatorMessage: OperatorMessage): Future[OperatorInstance] = {
    availableExecutor(topic)
      .flatMap(response => {
        val executor = SchedulerClient.clientSystem.actorOf(Props(classOf[ClientExecutor], response.executor.actor))
        executor
          .ask(ExecuteOperatorRequest(operatorMessage, CheckStateScheduled(FiniteDuration(10, "s"), StateHandler.empty())))
          .map(_ => OperatorInstance(executor))
      })
  }

  def execute(topic: String, operatorMessage: OperatorMessage, checkStateInterval: FiniteDuration, stateHandler: StateHandler): Future[OperatorInstance] = {
    availableExecutor(topic)
      .flatMap(response => {
        val executor = SchedulerClient.clientSystem.actorOf(Props(classOf[ClientExecutor], response.executor.actor))
        executor
          .ask(ExecuteOperatorRequest(operatorMessage, CheckStateScheduled(checkStateInterval, stateHandler)))
          .map(_ => OperatorInstance(executor))
      })
  }

  def availableExecutors(topic: String, timeWait: FiniteDuration): Future[AvailableExecutorsResponse] = {
    val client = getClient(executionContext)
    client
      .ask(AvailableExecutorsRequest(topic, timeWait))
      .mapTo[AvailableExecutorsResponse]
  }

  def availableExecutor(topic: String): Future[AvailableExecutorResponse] = {
    val client = getClient(executionContext)
    client
      .ask(AvailableExecutorRequest(topic))
      .mapTo[AvailableExecutorResponse]
  }
}

object SchedulerClient {
  val clientSystem: ActorSystem = ActorSystem(Environment.CLIENT_SYSTEM_NAME, ConfigFactory.load("client.conf"))

  def create(implicit executionContext: ExecutionContext, timeout: Timeout): SchedulerClient = {
    //先放一个contact-point, 系统会自动增加其它的点
    val initialContacts = Environment.CLUSTER_SEED_NODE.map(s => s + "/system/receptionist").map(ActorPaths.fromString).toSet

    val clusterClient = clientSystem.actorOf(
      ClusterClient.props(
        ClusterClientSettings(clientSystem)
          .withInitialContacts(initialContacts)),
      "client-endpoint")
    clientSystem.actorOf(Props(classOf[ClientListener], clusterClient), "client-event-listener")

    new SchedulerClient(clusterClient)
  }

  def main(args: Array[String]): Unit = {
    implicit val executionContext: ExecutionContextExecutor = ExecutionContext.global
    implicit val timeout: Timeout = 10 second
    val client = SchedulerClient.create
    val future = client
      .execute("test", OperatorMessage(
        "test",
        3,
        3000,
        1,
        PythonPropMessage(
          pyFile = "C:\\Users\\chenmingkun\\work\\idea\\work\\task-scheduler-core\\task-scheduler-executor\\src\\main\\resources\\test.py",
          args = Array("hello", "world"),
          env = Map("FILE_NAME" -> "test.py"),
          stdoutLineHandler = LineHandlerProducer.file("C:\\Users\\chenmingkun\\work\\idea\\work\\task-scheduler-core\\task-scheduler-executor\\src\\main\\resources\\test1.txt"),
          stderrLineHandler = LineHandlerProducer.file("C:\\Users\\chenmingkun\\work\\idea\\work\\task-scheduler-core\\task-scheduler-executor\\src\\main\\resources\\test2.txt"),
//          stdoutLineHandler = LineHandlerProducer.print(),
//          stderrLineHandler = LineHandlerProducer.print(),
          waitForTimeout = 100000
        )))
      future
      .onComplete {
        case Success(value) =>
          value.checkStateScheduled(2 second, StateHandler(state => println("state is: " + state)))
          value.onComplete(stat => {
            println("result is: " + stat)
            value.close()
          })
          Thread.sleep(12000)
          value.kill()
        case Failure(exception) =>
          exception.printStackTrace()
      }
    Thread.sleep(100000)
    clientSystem.terminate()
  }
}
