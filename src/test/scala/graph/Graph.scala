package graph

import akka.actor.ActorSystem
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{GraphDSL, _}
import akka.stream.{ClosedShape, SinkShape, UniformFanInShape}
import akka.{Done, NotUsed}
import com.oceanum.client.SchedulerClient
import com.oceanum.common.Environment
import com.oceanum.graph.{FlowFactory, GraphMeta}
import com.oceanum.utils.Test

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Graph extends App {
  Environment.loadArgs(Array("--conf=src\\main\\resources\\application.properties"))
  Environment.print()
  implicit lazy val sys: ActorSystem = Environment.FILE_SERVER_SYSTEM
  private val g = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
    val in = Source(0 to 10).map(_.toString)
    val out = Sink.foreach(println)

//    val bcast = builder.add(Broadcast[String](2))
    val merge = builder.add(Merge[String](3))
    val partition = builder.add(Partition[String](3, s => s.toInt % 3))
    val f1: Flow[String, String, NotUsed] = Flow[String].map(_ + "f0")
    val f2 = Flow[String].map(_ + "f1")
    val f3 = Flow[String].map(_ + "f2")

    in ~> partition
    partition.out(1) ~> f2 ~> merge
    partition.out(0) ~> f1 ~> merge
    partition.out(2) ~> f3 ~> merge
    merge ~> out
    ClosedShape
  })

  g.run()
}


object Test2 extends App {
  Environment.loadArgs(Array("--conf=src\\main\\resources\\application.properties"))
  Environment.print()
  implicit lazy val sys: ActorSystem = Environment.FILE_SERVER_SYSTEM

  val pickMaxOfThree = GraphDSL.create() { implicit b =>
    import GraphDSL.Implicits._

    val zip1 = b.add(ZipWith[Int, Int, Int](math.max))
    val zip2 = b.add(ZipWith[Int, Int, Int](math.max))
    zip1.out ~> zip2.in0
    UniformFanInShape(zip2.out, zip1.in0, zip1.in1, zip2.in1)
  }

  val resultSink = Sink.foreach(println)

  val g: RunnableGraph[Future[Done]] = RunnableGraph.fromGraph(GraphDSL.create(resultSink) { implicit b =>sink =>
    import GraphDSL.Implicits._

    // importing the partial graph will return its shape (inlets & outlets)
    val pm3: UniformFanInShape[Int, Int] = b.add(ZipWithN[Int, Int](_.sum)(3))
    val flow: Flow[Int, Int, NotUsed] = Flow[Int].map(_ + 1)
    Source(0 to 9) ~> flow ~> pm3.in(0)
    Source(0 to 11) ~> pm3.in(1)
    Source(0 to 12) ~> pm3.in(2)
    pm3.out ~> sink.in
    ClosedShape
  })
  g.run().onComplete(println)
}

object Test3 extends App {
  implicit val client: SchedulerClient = Test.client
  implicit val system: ActorSystem = Environment.CLIENT_SYSTEM
  val resultSink: Sink[GraphMeta, Future[Done]] = Sink.foreach[GraphMeta](m => println(m))
  val source: Source[GraphMeta, NotUsed] = Source.single(GraphMeta())
  val graph: RunnableGraph[Future[Done]] = RunnableGraph fromGraph GraphDSL.create(resultSink) { implicit builder: GraphDSL.Builder[Future[Done]] => sink =>
    val broadcast = FlowFactory.broadcast(2)
    val zip = FlowFactory.zip(2)
    val task1 = FlowFactory.create(Test.task("task1"))
    val task2 = FlowFactory.create(Test.task("task2"))
    source ~> broadcast ~> task1 ~> zip ~> sink
              broadcast ~> task2 ~> zip
    ClosedShape
  }

  graph.run()

  val graph2 = RunnableGraph fromGraph GraphDSL.create(resultSink) { implicit builder: GraphDSL.Builder[Future[Done]] => sink =>
    val broadcast = FlowFactory.broadcast(2)
    val zip = FlowFactory.zip(2)
    val task1 = Flow[GraphMeta].mapAsync(0)(m => Future.successful(m))
    val task2 = FlowFactory.create(Test.task("task2"))
    broadcast ~> task1 ~> zip
    broadcast ~> task2 ~> zip
    source ~> broadcast
    zip ~> sink
    ClosedShape
  }

  def create(func: (GraphDSL.Builder[Future[Done]], Source[GraphMeta, NotUsed], SinkShape[GraphMeta]) => Unit) = {
    implicit val client: SchedulerClient = Test.client
    implicit val system: ActorSystem = Environment.CLIENT_SYSTEM
    val resultSink: Sink[GraphMeta, Future[Done]] = Sink.foreach[GraphMeta](m => println(m))
    val source: Source[GraphMeta, NotUsed] = Source.single(GraphMeta())
    val graph: RunnableGraph[Future[Done]] = RunnableGraph fromGraph GraphDSL.create(resultSink) { builder => sink =>
        func(builder, source, sink)
        ClosedShape
    }

  }
}