package com.oceanum.Test

import java.net.{InetAddress, UnknownHostException}

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
      println("graphMeta: " + graphMeta)
    }

    override def onComplete(graphMeta: GraphMeta): Unit = {
      println("graphMeta complete: " + graphMeta.graphStatus)
      println(graphMeta)
      graphMeta.tasks.foreach(println)
      if (!promise.isCompleted)
        promise.success(graphMeta.asInstanceOf[RichGraphMeta])
    }

    override def onStart(graphMeta: GraphMeta): Unit = {
      println("graphMeta start: " + graphMeta)
    }

    override def close(): Unit = println("handler closed")
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

      start --> fork --> python1 --> join --> python5 --> end
      fork --> python2 --> decision --> python3 --> converge --> join
      decision --> python4 --> converge

    }.run()


    instance.offer(new RichGraphMeta() addEnv ("file_name" -> "python"))

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


object Graph3 {
  val promise = Graph.promise

  implicit val client: TaskClient = Graph.client

  implicit val metaHandler: GraphMetaHandler = Graph.metaHandler

  def main(args: Array[String]): Unit = {

    val instance = Workflow.fromGraph(GraphDefine(
      nodes = Map(
        "python1" -> TaskOperator(Test.task("1").copy(rawEnv = GraphContext(Map("file_name" -> "${(graph.id() % 2 == 0) ? 'python-err' : 'python'}")))),
        "python2" -> TaskOperator(Test.task("2")),
        "python3" -> TaskOperator(Test.task("3")),
        "python4" -> TaskOperator(Test.task("4")),
        "python5" -> TaskOperator(Test.task("5")),
        "fork" -> Fork(2),
        "join" -> Join(2),
        "decision" -> Decision(Array("false")),
        "converge" -> Converge(2),
        "start" -> Start(),
        "end" -> End()
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
      ),
      env = Map("file_name" -> "python")
    )).run()


    instance.offer(new RichGraphMeta())

    import scala.concurrent.ExecutionContext.Implicits.global
    promise.future.onComplete(meta => {
      Thread.sleep(3000)
      val m = meta.get.copy(reRunStrategy = ReRunStrategy.RUN_ALL_AFTER_FAILED)
      println("retry: " + m)
      instance.offer(m)
    })

  }
}

object Graph4 {
  val promise = Graph.promise

  implicit val client: TaskClient = Graph.client

  implicit val metaHandler: GraphMetaHandler = Graph.metaHandler

  def main(args: Array[String]): Unit = {

    val instance = Workflow.fromJson(
      """
        |  {
        |    "nodes" : {
        |      "join" : {
        |        "parallelism" : 2,
        |        "operatorType" : "Join"
        |      },
        |      "python2" : {
        |        "task" : {
        |          "id" : 4,
        |          "topic" : "default",
        |          "user" : "root",
        |          "retryCount" : 3,
        |          "retryInterval" : "5 second",
        |          "priority" : 5,
        |          "checkStateInterval" : "3s",
        |          "prop" : {
        |            "pyFile" : "/tmp/task-test/${file_name}.py",
        |            "args" : [ "hello", "${task.id()}", "${task.startTime()}" ],
        |            "env" : { },
        |            "directory" : "",
        |            "waitForTimeout" : "100 second"
        |          },
        |          "parallelism" : 1,
        |          "rawEnv" : {
        |            "env" : { },
        |            "graphMeta" : null,
        |            "taskMeta" : null
        |          },
        |          "taskType" : "PYTHON"
        |        },
        |        "parallelism" : 1,
        |        "operatorType" : "TaskOperator"
        |      },
        |      "decision" : {
        |        "expr" : [ "false" ],
        |        "operatorType" : "Decision"
        |      },
        |      "python3" : {
        |        "task" : {
        |          "id" : 5,
        |          "topic" : "default",
        |          "user" : "root",
        |          "retryCount" : 3,
        |          "retryInterval" : "5 second",
        |          "priority" : 5,
        |          "checkStateInterval" : "3s",
        |          "prop" : {
        |            "pyFile" : "/tmp/task-test/${file_name}.py",
        |            "args" : [ "hello", "${task.id()}", "${task.startTime()}" ],
        |            "env" : { },
        |            "directory" : "",
        |            "waitForTimeout" : "100 second"
        |          },
        |          "parallelism" : 1,
        |          "rawEnv" : {
        |            "env" : { },
        |            "graphMeta" : null,
        |            "taskMeta" : null
        |          },
        |          "taskType" : "PYTHON"
        |        },
        |        "parallelism" : 1,
        |        "operatorType" : "TaskOperator"
        |      },
        |      "fork" : {
        |        "parallelism" : 2,
        |        "operatorType" : "Fork"
        |      },
        |      "python4" : {
        |        "task" : {
        |          "id" : 6,
        |          "topic" : "default",
        |          "user" : "root",
        |          "retryCount" : 3,
        |          "retryInterval" : "5 second",
        |          "priority" : 5,
        |          "checkStateInterval" : "3s",
        |          "prop" : {
        |            "pyFile" : "/tmp/task-test/${file_name}.py",
        |            "args" : [ "hello", "${task.id()}", "${task.startTime()}" ],
        |            "env" : { },
        |            "directory" : "",
        |            "waitForTimeout" : "100 second"
        |          },
        |          "parallelism" : 1,
        |          "rawEnv" : {
        |            "env" : { },
        |            "graphMeta" : null,
        |            "taskMeta" : null
        |          },
        |          "taskType" : "PYTHON"
        |        },
        |        "parallelism" : 1,
        |        "operatorType" : "TaskOperator"
        |      },
        |      "python5" : {
        |        "task" : {
        |          "id" : 7,
        |          "topic" : "default",
        |          "user" : "root",
        |          "retryCount" : 3,
        |          "retryInterval" : "5 second",
        |          "priority" : 5,
        |          "checkStateInterval" : "3s",
        |          "prop" : {
        |            "pyFile" : "/tmp/task-test/${file_name}.py",
        |            "args" : [ "hello", "${task.id()}", "${task.startTime()}" ],
        |            "env" : { },
        |            "directory" : "",
        |            "waitForTimeout" : "100 second"
        |          },
        |          "parallelism" : 1,
        |          "rawEnv" : {
        |            "env" : { },
        |            "graphMeta" : null,
        |            "taskMeta" : null
        |          },
        |          "taskType" : "PYTHON"
        |        },
        |        "parallelism" : 1,
        |        "operatorType" : "TaskOperator"
        |      },
        |      "end" : {
        |        "operatorType" : "End"
        |      },
        |      "converge" : {
        |        "parallelism" : 2,
        |        "operatorType" : "Converge"
        |      },
        |      "python1" : {
        |        "task" : {
        |          "id" : 3,
        |          "topic" : "default",
        |          "user" : "root",
        |          "retryCount" : 3,
        |          "retryInterval" : "5 second",
        |          "priority" : 5,
        |          "checkStateInterval" : "3s",
        |          "prop" : {
        |            "pyFile" : "/tmp/task-test/${file_name}.py",
        |            "args" : [ "hello", "${task.id()}", "${task.startTime()}" ],
        |            "env" : { },
        |            "directory" : "",
        |            "waitForTimeout" : "100 second"
        |          },
        |          "parallelism" : 1,
        |          "rawEnv" : {
        |            "env" : {
        |              "file_name" : "${(graph.id() % 2 == 0) ? 'python-err' : 'python'}"
        |            },
        |            "graphMeta" : null,
        |            "taskMeta" : null
        |          },
        |          "taskType" : "PYTHON"
        |        },
        |        "parallelism" : 1,
        |        "operatorType" : "TaskOperator"
        |      },
        |      "start" : {
        |        "operatorType" : "Start"
        |      }
        |    },
        |    "edges" : {
        |      "join" : [ "python5" ],
        |      "python2" : [ "decision" ],
        |      "decision" : [ "python3", "python4" ],
        |      "python3" : [ "converge" ],
        |      "fork" : [ "python1", "python2" ],
        |      "python4" : [ "converge" ],
        |      "python5" : [ "end" ],
        |      "converge" : [ "join" ],
        |      "python1" : [ "join" ],
        |      "start" : [ "fork" ]
        |    },
        |    "env" : {
        |      "file_name" : "python"
        |    }
        |  }
        |""".stripMargin
    ).run()


    instance.offer(new RichGraphMeta())

    import scala.concurrent.ExecutionContext.Implicits.global
    promise.future.onComplete(meta => {
      Thread.sleep(3000)
      val m = meta.get.copy(reRunStrategy = ReRunStrategy.RUN_ALL_AFTER_FAILED)
      println("retry: " + m)
      instance.offer(m)
    })

  }
}