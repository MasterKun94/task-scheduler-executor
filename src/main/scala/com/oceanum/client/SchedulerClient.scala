package com.oceanum.client

import java.io.File

import akka.actor.{ActorPaths, ActorRef, ActorSystem, Props}
import akka.cluster.client.{ClusterClient, ClusterClientSettings}
import akka.pattern.ask
import akka.util.Timeout
import com.oceanum.actors.{ExecutorFinder, ClientExecutor, ClientListener, StateHandler}
import com.oceanum.common.Environment
import com.oceanum.exec.LineHandler
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
    SchedulerClient.clientSystem.actorOf(Props(classOf[ExecutorFinder], endpoint, executionContext), "client-actor")
  }


  def execute(topic: String, task: Task, stateHandler: StateHandler = StateHandler.empty()): Future[ExecutorInstance] = {
    doExecute(AvailableExecutorRequest(topic), task, stateHandler)
  }

  def executeAll(topic: String, task: Task, stateHandler: StateHandler = StateHandler.empty())(implicit timeWait: FiniteDuration): Future[ExecutorInstance] = {
    doExecute(AvailableExecutorsRequest(topic, timeWait), task, stateHandler)
  }

  private def doExecute(requestMsg: Any, task: Task, stateHandler: StateHandler = StateHandler.empty()): Future[ExecutorInstance] = {
    val client = getClient(executionContext)
    client
      .ask(requestMsg)
      .mapTo[AvailableExecutorResponse]
      .flatMap(response => {
        val res = response.executor
          .map(executor => SchedulerClient.clientSystem.actorOf(Props(classOf[ClientExecutor], executor.actor)))
          .map(client => client
            .ask(ExecuteOperatorRequest(task, CheckStateScheduled(stateHandler.checkInterval(), stateHandler)))
            .map(_ => client)
          )
          .toSeq
        Future.sequence(res).map(ExecutorInstance(_))
      })
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
    val path = "C:\\Users\\chenmingkun\\work\\idea\\work\\task-scheduler-core\\task-scheduler-executor\\src\\main\\resources"
    implicit val executionContext: ExecutionContextExecutor = ExecutionContext.global
    implicit val timeout: Timeout = 10 second
    implicit val timeWait: FiniteDuration = 2 second
    val client = SchedulerClient.create
//    client.availableExecutors("test").map(res => res.executors.mkString(", \n")).foreach(println)
    val future = client
      .executeAll("a1", Task(
        "test",
        3,
        3000,
        1,
        PythonTaskProp(
          pyFile = s"$path\\test.py",
          args = Array("hello", "world"),
          env = Map("FILE_NAME" -> "test.py"),
          stdoutLineHandler = () => LineHandler.fileOutputHandler(new File(s"$path\\test1.txt")),
          stderrLineHandler = () => LineHandler.fileOutputHandler(new File(s"$path\\test2.txt")),
//          stdoutLineHandler = LineHandlerProducer.print(),
//          stderrLineHandler = LineHandlerProducer.print(),
          waitForTimeout = 100000
        )))
      future
      .onComplete {
        case Success(value) =>
          value.handleState(2 second, state => println("state is: " + state))
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
