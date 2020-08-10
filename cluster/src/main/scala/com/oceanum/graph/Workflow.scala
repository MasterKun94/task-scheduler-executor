package com.oceanum.graph

import java.util.Date
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.stream.{ClosedShape, OverflowStrategy}
import akka.stream.scaladsl.{GraphDSL, RunnableGraph, Sink, Source}
import com.oceanum.api.entities.WorkflowDefine
import com.oceanum.client.TaskClient
import com.oceanum.common.{Environment, GraphMeta, GraphStatus, RerunStrategy, RichGraphMeta}
import com.oceanum.exec.State
import com.oceanum.graph.StreamFlows.{EndFlow, StartFlow}
import com.oceanum.serialize.{DefaultJsonSerialization, Serialization}

import scala.collection.concurrent.TrieMap
import scala.util.{Failure, Success}

class Workflow(runnableGraph: Graph, graphMetaHandler: GraphMetaHandler) {
  def run()(implicit client: TaskClient): WorkflowInstance = {
    implicit val system: ActorSystem = client.system
    val (queue, future) = runnableGraph.run()
    val future0 = future.andThen {
      case Success(_) =>
        graphMetaHandler.close()
      case Failure(exception) =>
        exception.printStackTrace()
        graphMetaHandler.close()

    } (client.system.dispatcher)
    WorkflowInstance(queue, future0)
  }
}

object Workflow {
  def create(env: Map[String, Any], builder: GraphBuilder => Unit)(implicit taskClient: TaskClient, graphMetaHandler: GraphMetaHandler): Workflow = {
    val createTime = new Date()
    val metaHandler: GraphMetaHandler = new GraphMetaHandler {
      private val actor = taskClient.system.actorOf(Props(classOf[GraphMetaHandlerActor], Array(graphMetaHandler, GraphMetaHandler.default)))
      override def onRunning(graphMeta: GraphMeta, taskState: State): Unit = actor ! OnRunning(graphMeta, taskState)

      override def onComplete(graphMeta: GraphMeta): Unit = actor ! OnComplete(graphMeta)

      override def onStart(graphMeta: GraphMeta): Unit = actor ! OnStart(graphMeta)

      override def close(): Unit = actor ! PoisonPill
    }
    val idOffset = new AtomicInteger(-1)
    val source0 = Source
      .queue[Message](Environment.GRAPH_SOURCE_QUEUE_BUFFER_SIZE, OverflowStrategy.backpressure)
      .map { message =>
        val meta = message.meta

        val start =
          if (meta.rerunStrategy == RerunStrategy.NONE) {
            if (idOffset.get() >= meta.id) {
              meta.copy(
                error = Option(new IllegalArgumentException("id is not legal, must higher than " + idOffset.get())),
                graphStatus = GraphStatus.FAILED
              )
            } else {
              idOffset.set(meta.id)
              meta.copy(
                createTime = Some(createTime),
                startTime = Some(new Date()),
                endTime = None,
                graphStatus = GraphStatus.RUNNING,
                rerunId = 0,
                rerunFlag = false,
                env = env ++ meta.env,
                latestTaskId = -1
              )
            }
          } else {
            meta.copy(
              createTime = Some(createTime),
              startTime = Some(new Date()),
              endTime = None,
              graphStatus = GraphStatus.RUNNING,
              rerunId = meta.rerunId + 1,
              rerunFlag = false,
              env = env ++ meta.env,
              latestTaskId = -1
            )
          }

        metaHandler.onStart(start)
        Message(start, message.instances)
      }

    val sink0 = Sink.foreach[Message](message => metaHandler.onComplete(message.meta))
    val graph = GraphDSL.create(source0, sink0)(_ -> _) { implicit b => (source, sink) =>
      val start = StartFlow(source)
      val end = EndFlow(sink)
      builder(GraphBuilder(start, end, metaHandler, b))
      ClosedShape
    }
    new Workflow(RunnableGraph.fromGraph(graph), metaHandler)
  }

  def create(builder: GraphBuilder => Unit)(implicit taskClient: TaskClient, graphMetaHandler: GraphMetaHandler = GraphMetaHandler.empty): Workflow = {
    create(Map.empty, builder)
  }

  def fromGraph(workflowDefine: WorkflowDefine)(implicit taskClient: TaskClient, graphMetaHandler: GraphMetaHandler = GraphMetaHandler.empty): Workflow = {
    create(workflowDefine.env, implicit builder => {
      val dag = workflowDefine.dag
      val operators = dag.vertexes.mapValues(Operators.from)
      for ((prim, subs) <- dag.edges) {
        val parent = operators(prim)
        val children = subs.map(operators)
        builder.buildEdges(parent, children:_*)
      }
      builder.createGraph()
    }
    )
  }

  def fromJson(json: String)(implicit taskClient: TaskClient, graphMetaHandler: GraphMetaHandler = GraphMetaHandler.empty): Workflow = {
    fromGraph(Serialization.default.deSerialize(json).asInstanceOf[WorkflowDefine])
  }
}