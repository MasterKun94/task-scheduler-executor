package com.oceanum.Test

import java.net.{InetAddress, UnknownHostException}

import com.oceanum.ClusterStarter
import com.oceanum.Test.Graph.getSelfAddress
import com.oceanum.client.TaskClient
import com.oceanum.cluster.exec.State
import com.oceanum.common.Environment.Arg
import com.oceanum.graph.{GraphMetaHandler, ReRunStrategy, RichGraphMeta}

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
    override def onRunning(richGraphMeta: RichGraphMeta, taskState: State): Unit = {
      println("state: " + taskState)
      println("graphMeta: " + richGraphMeta)
    }

    override def onComplete(richGraphMeta: RichGraphMeta): Unit = {
      println("graphMeta complete: " + richGraphMeta.graphStatus)
      println(richGraphMeta)
      richGraphMeta.operators.foreach(println)
      if (!promise.isCompleted) promise.success(richGraphMeta)
    }

    override def onStart(richGraphMeta: RichGraphMeta): Unit = {
      println("graphMeta start: " + richGraphMeta)
    }

    override def close(): Unit = println("handler closed")
  }

  def main(args: Array[String]): Unit = {
    import com.oceanum.graph.FlowFactory._

    val instance = createGraph { implicit graph =>

      val python1 = createFlow(Test.task().copy(rawEnv = Map("file_name" -> "${(graph.id() % 2 == 0) ? 'python-err' : 'python'}")))
      val python2 = createFlow(Test.task())
      val python3 = createFlow(Test.task())
      val python4 = createFlow(Test.task())
      val python5 = createFlow(Test.task())
      val fork = createFork(2)
      val join = createJoin(2)
      val decision = createDecision(Array("false"))
      val converge = createConverge(2)

      graph.start --> fork
      fork --> python1 --> join
      fork --> python2 --> decision
      decision --> python3 --> converge
      decision --> python4 --> converge
      converge --> join
      join --> python5 --> graph.end

    }.run()


    instance.offer(RichGraphMeta() addEnv ("file_name" -> "python"))

    import scala.concurrent.ExecutionContext.Implicits.global
    promise.future.onComplete(meta => {
      Thread.sleep(3000)
      val m = meta.get.reRunStrategy = ReRunStrategy.RUN_ALL_AFTER_FAILED
      println("retry: " + m)
      instance.offer(m)
    })

  }
}

object Test1 extends App {
  val configFile = "arc/main/resources/application.properties"
  val ip2 = getSelfAddress
  ClusterStarter.main(Array(s"${Arg.CONF}=$configFile", s"${Arg.HOST}=$ip2"))
}