package graph

import akka.{Done, NotUsed}
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.{ClosedShape, OverflowStrategy}
import akka.stream.scaladsl._
import com.oceanum.client.SchedulerClient
import com.oceanum.graph.{GraphMeta, RichGraphMeta, Workflow}
import com.oceanum.utils.Test

import scala.concurrent.Future

object Graph extends App {
  implicit val client: SchedulerClient = Test.client
  import com.oceanum.graph.FlowFactory._

  val (actor, future) = createGraph { implicit graph =>

    val task1 = createFlow(Test.task("task1"))
    val task2 = createFlow(Test.task("task2"))
    val task3 = createFlow(Test.task("task3"))
    val task4 = createFlow(Test.task("task4"))
    val task5 = createFlow(Test.task("task5"))
    val fork = createFork(2)
    val join = createJoin(2)
    val decision = createDecision(2)(_ => 1)
    val converge = createConverge(2)

    graph.start --> fork
    fork --> task1 --> join
    fork --> task2 --> decision
    decision --> task3 --> converge
    decision --> task4 --> converge
    converge --> task5 --> join
    join --> graph.end

  }.run()


    actor.offer(RichGraphMeta())

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