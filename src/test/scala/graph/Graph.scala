package graph

import akka.actor.ActorSystem
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{GraphDSL, _}
import akka.stream.{ClosedShape, Outlet, SinkShape, UniformFanInShape, UniformFanOutShape}
import akka.{Done, NotUsed}
import com.oceanum.client.SchedulerClient
import com.oceanum.common.Environment
import com.oceanum.graph.{FlowFactory, GraphBuilder, GraphMeta}
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

  create(
    new GraphBuilder {
      override def build(implicit source: Source[GraphMeta, NotUsed], sink: SinkShape[GraphMeta], builder: GraphDSL.Builder[Future[Done]]): Unit = {
        val task1: Flow[GraphMeta, GraphMeta, NotUsed] = FlowFactory.create(Test.task("task1"))
        val task2: Flow[GraphMeta, GraphMeta, NotUsed] = FlowFactory.create(Test.task("task2"))
        val broadcast: UniformFanOutShape[GraphMeta, GraphMeta] = FlowFactory.broadcast(2)
        val zip: UniformFanInShape[GraphMeta, GraphMeta] = FlowFactory.zip(2)
        val choose: UniformFanOutShape[GraphMeta, GraphMeta] = FlowFactory.partition(1)(m => 0)
        val out: Outlet[GraphMeta] = choose.out(0)
        val o: PortOps[GraphMeta] = broadcast ~> task1
        val v: PortOps[GraphMeta] = source ~> broadcast
        broadcast.out(0) ~> task1
        val e: PortOps[GraphMeta] = broadcast ~> zip
        val t: PortOps[GraphMeta] = source ~> task1
        val m: Unit = broadcast ~> sink
        val p = v ~> sink
        source ~> broadcast
        broadcast ~> task1 ~> zip
        broadcast ~> task2 ~> zip
        zip ~> sink
      }
    })
    .run()

  def create(builder: GraphBuilder): RunnableGraph[Future[Done]] = {
    implicit val client: SchedulerClient = Test.client
    implicit val system: ActorSystem = Environment.CLIENT_SYSTEM
    val resultSink: Sink[GraphMeta, Future[Done]] = Sink.foreach[GraphMeta](m => println(m))
    val source: Source[GraphMeta, NotUsed] = Source.single(GraphMeta())
    RunnableGraph fromGraph GraphDSL.create(resultSink) { b => sink =>
        builder.build(source, sink, b)
        ClosedShape
    }
  }
}