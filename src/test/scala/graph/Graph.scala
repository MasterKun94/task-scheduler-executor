package graph

import akka.Done
import akka.actor.ActorSystem
import akka.stream.scaladsl._
import akka.stream.{ClosedShape, OverflowStrategy}
import com.oceanum.client.TaskClient
import com.oceanum.graph.{GraphMetaHandler, RichGraphMeta}
import com.oceanum.utils.Test

import scala.concurrent.Future

object Graph extends App {
  implicit val client: TaskClient = Test.client
  implicit val metaHandler: GraphMetaHandler = GraphMetaHandler.default()
  import com.oceanum.graph.FlowFactory._

  val instance = createGraph { implicit graph =>

    val python1 = createFlow(Test.task("/tmp/task-test/${file_name}.py"))
    val python2 = createFlow(Test.task("/tmp/task-test/${file_name}.py"))
    val python3 = createFlow(Test.task("/tmp/task-test/${file_name}.py"))
    val python4 = createFlow(Test.task("/tmp/task-test/${file_name}.py"))
    val python5 = createFlow(Test.task("/tmp/task-test/${file_name}.py").copy(env = Map("file_name" -> "python-err")))
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