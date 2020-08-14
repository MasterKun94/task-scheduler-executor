import java.net.{InetAddress, UnknownHostException}

import com.oceanum.api.entities._
import com.oceanum.client.TaskClient
import com.oceanum.common.{GraphContext, GraphMeta, RerunStrategy, RichGraphMeta}
import com.oceanum.exec.State
import com.oceanum.graph.{GraphMetaHandler, Workflow}
import com.oceanum.serialize.Serialization

import scala.concurrent.Promise

object Graph {
  val ip2 = "192.168.10.131"
  val ip1 = try {
    InetAddress.getLocalHost.getHostAddress
  } catch {
    case _: Throwable => "127.0.0.1"
  }

  implicit val client: TaskClient = TaskClient(ip1, 5552, ip2, "cluster/src/main/resources/application.properties")

  implicit val metaHandler: GraphMetaHandler = new GraphMetaHandler {
    override def onRunning(graphMeta: GraphMeta, taskState: State): Unit = {
      println("state: " + taskState)
    }

    override def onComplete(graphMeta: GraphMeta): Unit = {
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

      val python1 = createFlow(TaskTest.task().copy(rawEnv = GraphContext(Map("file_name" -> "${(graph.reRunId() % 2 == 0) ? 'python-err' : 'python'}"))))
      val python2 = createFlow(TaskTest.task())
      val python3 = createFlow(TaskTest.task())
      val python4 = createFlow(TaskTest.task())
      val python5 = createFlow(TaskTest.task())
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
  }
}


object Graph2 {

  implicit val client: TaskClient = Graph.client
  implicit val metaHandler: GraphMetaHandler = Graph.metaHandler

  def main(args: Array[String]): Unit = {

    val instance = Workflow.create { implicit graph => import graph._

      val python1 = createFlow(TaskTest.task().copy(rawEnv = GraphContext(Map("file_name" -> "${(graph.id() % 2 == 0) ? 'python-err' : 'python'}"))))
      val python2 = createFlow(TaskTest.task())
      val python3 = createFlow(TaskTest.task())
      val python4 = createFlow(TaskTest.task())
      val python5 = createFlow(TaskTest.task())
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
  }
}


object Graph3 {
  implicit val client: TaskClient = Graph.client
  implicit val metaHandler: GraphMetaHandler = Graph.metaHandler

  def main(args: Array[String]): Unit = {

    val instance = Workflow.fromGraph(WorkflowDefine(
      version = 1,
      name = "",
      Dag(
        vertexes = Map(
          "python1" -> TaskVertex(TaskTest.task("1").copy(rawEnv = GraphContext(Map("file_name" -> "${(graph.id() % 2 == 0) ? 'python-err' : 'python'}")))),
          "python2" -> TaskVertex(TaskTest.task("2")),
          "python3" -> TaskVertex(TaskTest.task("3")),
          "python4" -> TaskVertex(TaskTest.task("4")),
          "python5" -> TaskVertex(TaskTest.task("5")),
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
      ),
      env = Map("file_name" -> "python")
    )).run()

    instance.offer(new RichGraphMeta().copy(id = 0))
  }
}