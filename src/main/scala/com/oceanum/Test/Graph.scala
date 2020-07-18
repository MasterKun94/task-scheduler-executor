package com.oceanum.Test

import java.net.{InetAddress, UnknownHostException}

import akka.Done
import akka.actor.ActorSystem
import akka.stream.scaladsl._
import akka.stream.{ClosedShape, OverflowStrategy}
import com.oceanum.client.{Task, TaskClient}
import com.oceanum.graph.{GraphMetaHandler, RichGraphMeta}

import scala.concurrent.Future

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

  implicit val client: TaskClient = TaskClient(ip1, 5555, ip2, "src/main/resources/application.properties")
  implicit val metaHandler: GraphMetaHandler = GraphMetaHandler.default()

  def main(args: Array[String]): Unit = {
    import com.oceanum.graph.FlowFactory._

    val instance = createGraph { implicit graph =>

      val python1 = createFlow(Test.task())
      val python2 = createFlow(Test.task())
      val python3 = createFlow(Test.task())
      val python4 = createFlow(Test.task())
      val python5 = createFlow(Test.task().copy(rawEnv = Map("file_name" -> "python-err")))
      val fork = createFork(2)
      val join = createJoin(2)
      val decision = createDecision(2)(_ => 1)
      val converge = createConverge(2)

      graph.start --> fork
      fork --> python1 --> join
      fork --> python2 --> decision
      decision --> python3 --> converge
      decision --> python4 --> converge
      converge --> python5 --> join
      join --> graph.end

    }.run()


    instance.offer(RichGraphMeta() addEnv ("file_name" -> "python"))

  }
}

object Test1 extends App {
  implicit val sys: ActorSystem = Test.client.system
  import scala.concurrent.ExecutionContext.Implicits.global
  val source0 = Source.queue[RichGraphMeta](1000, OverflowStrategy.backpressure)
  val sink0 = Sink.foreach[RichGraphMeta]{ metadata => {
    println(metadata.graphStatus)
    println(metadata.operators.mkString("\r\n"))
  }}
  val graph = RunnableGraph.fromGraph(GraphDSL.create(source0, sink0)((_, _)) { implicit builder: GraphDSL.Builder[(SourceQueueWithComplete[RichGraphMeta], Future[Done])] =>(source, sink) =>
//    val start = Start(source)
//    val end = End(sink)
    import GraphDSL.Implicits._
    val flow = Flow[RichGraphMeta].mapAsync(1)(m => Future({Thread.sleep(3000); println(m); m}))
    source ~> flow ~> sink
    ClosedShape
  })
    .run()

  for (e <- 1 to 5) {
    Thread.sleep(2000)
    println("start")
    graph._1.offer(RichGraphMeta())
  }
}