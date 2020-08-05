package com.oceanum.Test

import java.net.{InetAddress, UnknownHostException}

import com.oceanum.api.entities.{ConvergeVertex, Dag, DecisionVertex, EndVertex, ForkVertex, JoinVertex, StartVertex, TaskVertex, WorkflowDefine}
import com.oceanum.client.TaskClient
import com.oceanum.common.{GraphContext, GraphMeta, ReRunStrategy, RichGraphMeta}
import com.oceanum.exec.State
import com.oceanum.graph.Operators._
import com.oceanum.graph.{GraphDefine, GraphMetaHandler, Workflow}
import com.oceanum.serialize.{DefaultJsonSerialization, Serialization}

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

  val promise: Promise[RichGraphMeta] = Promise[RichGraphMeta]()

  implicit val client: TaskClient = TaskClient(ip1, 5552, ip2, "cluster/src/main/resources/application.properties")

  implicit val metaHandler: GraphMetaHandler = new GraphMetaHandler {
    override def onRunning(graphMeta: GraphMeta, taskState: State): Unit = {
      println("state: " + taskState)
    }

    override def onComplete(graphMeta: GraphMeta): Unit = {
      if (!promise.isCompleted)
        promise.success(RichGraphMeta(graphMeta))
      println("graphMeta complete: " + graphMeta.graphStatus)
      graphMeta.tasks.foreach(println)
    }

    override def onStart(graphMeta: GraphMeta): Unit = {
      println("graphMeta start: " + graphMeta)
    }

    override def close(): Unit = {
      println("handler closed")
    }
  }

  def main(args: Array[String]): Unit = {

    val instance = Workflow.create { implicit graph => import graph.flowFactory._

      val python1 = createFlow(Test.task().copy(rawEnv = GraphContext(Map("file_name" -> "${(graph.reRunId() % 2 == 0) ? 'python-err' : 'python'}"))))
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

      start --> fork --> python1 -->                                       join --> python5 --> end
                fork --> python2 --> decision --> python3 --> converge --> join
                                     decision --> python4 --> converge

    }.run()


    instance.offer(new RichGraphMeta().copy(id = 0) addEnv ("file_name" -> "python"))

    import scala.concurrent.ExecutionContext.Implicits.global
    promise.future.onComplete(meta => {
      Thread.sleep(3000)

      val ctx = new GraphContext(Map.empty).copy(graphMeta = meta.get)
      val str = Serialization.default.serialize(ctx)
      println(str)
      val c = Serialization.default.deSerialize(str).asInstanceOf[GraphContext]
      println(c)
      println(c.graphMeta)

      val m = meta.get.copy(reRunStrategy = ReRunStrategy.RUN_ALL_AFTER_FAILED)
      println("retry: " + m)
      instance.offer(m)
    })

  }
}


object Graph2 {

  val promise = Graph.promise

  implicit val client: TaskClient = Graph.client

  implicit val metaHandler: GraphMetaHandler = Graph.metaHandler

  def main(args: Array[String]): Unit = {

    val instance = Workflow.create { implicit graph => import graph._

      val python1 = createFlow(Test.task().copy(rawEnv = GraphContext(Map("file_name" -> "${(graph.id() % 2 == 0) ? 'python-err' : 'python'}"))))
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


    instance.offer(new RichGraphMeta().copy(id = 0) addEnv ("file_name" -> "python"))

    import scala.concurrent.ExecutionContext.Implicits.global
    promise.future.onComplete(meta => {
      Thread.sleep(3000)
      val m = meta.get.copy(reRunStrategy = ReRunStrategy.RUN_ALL_AFTER_FAILED)
      println("retry: " + m)
      instance.offer(m)
    })

  }
}


object Graph3 {
  val promise = Graph.promise

  implicit val client: TaskClient = Graph.client

  implicit val metaHandler: GraphMetaHandler = Graph.metaHandler

  def main(args: Array[String]): Unit = {

    val instance = Workflow.fromGraph(WorkflowDefine(
      version = 1,
      name = "",
      Dag(
        vertexes = Map(
          "python1" -> TaskVertex(Test.task("1").copy(rawEnv = GraphContext(Map("file_name" -> "${(graph.id() % 2 == 0) ? 'python-err' : 'python'}")))),
          "python2" -> TaskVertex(Test.task("2")),
          "python3" -> TaskVertex(Test.task("3")),
          "python4" -> TaskVertex(Test.task("4")),
          "python5" -> TaskVertex(Test.task("5")),
          "fork" -> ForkVertex(2),
          "join" -> JoinVertex(2),
          "decision" -> DecisionVertex(Array("false")),
          "converge" -> ConvergeVertex(2),
          "start" -> StartVertex(),
          "end" -> EndVertex()
        ),
        edges = Map(
          "start" -> Array("fork"),
          "fork" -> Array("python1", "python2"),
          "python1" -> Array("join"),
          "python2" -> Array("decision"),
          "decision" -> Array("python3", "python4"),
          "python3" -> Array("converge"),
          "python4" -> Array("converge"),
          "converge" -> Array("join"),
          "join" -> Array("python5"),
          "python5" -> Array("end")
        )
      )
      ,
      env = Map("file_name" -> "python")
    )).run()


    instance.offer(new RichGraphMeta().copy(id = 0))

    import scala.concurrent.ExecutionContext.Implicits.global
    promise.future.onComplete(meta => {
      Thread.sleep(3000)
      val m = meta.get.copy(reRunStrategy = ReRunStrategy.RUN_ALL_AFTER_FAILED)
      println("retry: " + m)
      instance.offer(m)
    })

  }
}