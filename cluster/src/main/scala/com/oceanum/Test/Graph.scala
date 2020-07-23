package com.oceanum.Test

import java.net.{InetAddress, UnknownHostException}

import com.oceanum.client.TaskClient
import com.oceanum.common.{ExprContext, GraphMeta, RichGraphMeta}
import com.oceanum.exec.State
import com.oceanum.graph.{FlowFactory, GraphMetaHandler, ReRunStrategy, WorkflowRunner}

import scala.concurrent.Promise

object Graph {
  val ip2 = "192.168.10.131"
  val ip1 = getSelfAddress
  def getSelfAddress: String = {
    try {
      InetAddress.getLocalHost.getHostAddress
    } catch {
      case _: UnknownHostException => "127.0.0.1"
      case e: Throwable => throw e
    }
  }

  val promise = Promise[RichGraphMeta]()

  implicit val client: TaskClient = TaskClient(ip1, 5552, ip2, "src/main/resources/application.properties")

  implicit val metaHandler: GraphMetaHandler = new GraphMetaHandler {
    override def onRunning(richGraphMeta: GraphMeta, taskState: State): Unit = {
      println("state: " + taskState)
      println("graphMeta: " + richGraphMeta)
    }

    override def onComplete(richGraphMeta: GraphMeta): Unit = {
      println("graphMeta complete: " + richGraphMeta.graphStatus)
      println(richGraphMeta)
      richGraphMeta.operators.foreach(println)
      if (!promise.isCompleted) promise.success(richGraphMeta.asInstanceOf[RichGraphMeta])
    }

    override def onStart(richGraphMeta: GraphMeta): Unit = {
      println("graphMeta start: " + richGraphMeta)
    }

    override def close(): Unit = println("handler closed")
  }

  def main(args: Array[String]): Unit = {

    val instance = WorkflowRunner.createGraph { implicit graph => import graph.flowFactory._

      val python1 = createFlow(Test.task().copy(rawEnv = ExprContext(Map("file_name" -> "${(graph.id() % 2 == 0) ? 'python-err' : 'python'}"))))
      val python2 = createFlow(Test.task())
      val python3 = createFlow(Test.task())
      val python4 = createFlow(Test.task())
      val python5 = createFlow(Test.task())
      val fork = createFork(2)
      val join = createJoin(2)
      val decision = createDecision(Array("false"))
      val converge = createConverge(2)

      createStart --> fork
      fork --> python1 --> join
      fork --> python2 --> decision
      decision --> python3 --> converge
      decision --> python4 --> converge
      converge --> join
      join --> python5 --> createEnd

    }.run()


    instance.offer(new RichGraphMeta() addEnv ("file_name" -> "python"))

    import scala.concurrent.ExecutionContext.Implicits.global
    promise.future.onComplete(meta => {
      Thread.sleep(3000)
      val m = meta.get.copy(reRunStrategy = ReRunStrategy.RUN_ALL_AFTER_FAILED)
      println("retry: " + m)
      instance.offer(m)
    })

  }
}


object Graph2 {
  val ip2 = "192.168.10.131"
  val ip1 = getSelfAddress
  def getSelfAddress: String = {
    try {
      InetAddress.getLocalHost.getHostAddress
    } catch {
      case _: UnknownHostException => "127.0.0.1"
      case e: Throwable => throw e
    }
  }

  val promise = Promise[RichGraphMeta]()

  implicit val client: TaskClient = TaskClient(ip1, 5552, ip2, "src/main/resources/application.properties")

  implicit val metaHandler: GraphMetaHandler = new GraphMetaHandler {
    override def onRunning(richGraphMeta: GraphMeta, taskState: State): Unit = {
      println("state: " + taskState)
      println("graphMeta: " + richGraphMeta)
    }

    override def onComplete(richGraphMeta: GraphMeta): Unit = {
      println("graphMeta complete: " + richGraphMeta.graphStatus)
      println(richGraphMeta)
      richGraphMeta.operators.foreach(println)
      if (!promise.isCompleted) promise.success(richGraphMeta.asInstanceOf[RichGraphMeta])
    }

    override def onStart(richGraphMeta: GraphMeta): Unit = {
      println("graphMeta start: " + richGraphMeta)
    }

    override def close(): Unit = println("handler closed")
  }

  def main(args: Array[String]): Unit = {

    val instance = WorkflowRunner.createGraph { implicit graph => import graph._

      val python1 = createFlow(Test.task().copy(rawEnv = ExprContext(Map("file_name" -> "${(graph.id() % 2 == 0) ? 'python-err' : 'python'}"))))
      val python2 = createFlow(Test.task())
      val python3 = createFlow(Test.task())
      val python4 = createFlow(Test.task())
      val python5 = createFlow(Test.task())
      val fork = createFork(2)
      val join = createJoin(2)
      val decision = createDecision(Array("false"))
      val converge = createConverge(2)
      val start = createStart
      val end = createEnd

      buildEdges(start, fork)
      buildEdges(fork, python1, python2)
      buildEdges(python1, join)
      buildEdges(python2, decision)
      buildEdges(decision, python3, python4)
      buildEdges(python3, converge)
      buildEdges(python4, converge)
      buildEdges(converge, join)
      buildEdges(join, python5)
      buildEdges(python5, end)

      createGraph()
    }.run()


    instance.offer(new RichGraphMeta() addEnv ("file_name" -> "python"))

    import scala.concurrent.ExecutionContext.Implicits.global
    promise.future.onComplete(meta => {
      Thread.sleep(3000)
      val m = meta.get.copy(reRunStrategy = ReRunStrategy.RUN_ALL_AFTER_FAILED)
      println("retry: " + m)
      instance.offer(m)
    })

  }
}