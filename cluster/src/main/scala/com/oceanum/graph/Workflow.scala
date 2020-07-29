package com.oceanum.graph

import java.util.Date
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.stream.{ClosedShape, OverflowStrategy}
import akka.stream.scaladsl.{GraphDSL, RunnableGraph, Sink, Source}
import com.oceanum.client.TaskClient
import com.oceanum.common.{Environment, GraphMeta, GraphStatus, ReRunStrategy, RichGraphMeta}
import com.oceanum.exec.State
import com.oceanum.graph.StreamFlows.{EndFlow, StartFlow}
import com.oceanum.serialize.DefaultJsonSerialization

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
  def create(env: Map[String, Any], builder: GraphBuilder => Unit)
            (implicit taskClient: TaskClient, graphMetaHandler: GraphMetaHandler = GraphMetaHandler.default()): Workflow = {
    val metaHandler: GraphMetaHandler = new GraphMetaHandler {
      private val actor = taskClient.system.actorOf(Props(classOf[GraphMetaHandlerActor], graphMetaHandler))
      override def onRunning(graphMeta: GraphMeta, taskState: State): Unit = actor ! OnRunning(graphMeta, taskState)

      override def onComplete(graphMeta: GraphMeta): Unit = actor ! OnComplete(graphMeta)

      override def onStart(graphMeta: GraphMeta): Unit = actor ! OnStart(graphMeta)

      override def close(): Unit = actor ! PoisonPill
    }
    val atomicInteger = new AtomicInteger(0)
    val source0 = Source
      .queue[RichGraphMeta](Environment.GRAPH_SOURCE_QUEUE_BUFFER_SIZE, OverflowStrategy.backpressure)
      .map { meta =>
        val start =
          if (meta.reRunStrategy == ReRunStrategy.NONE) {
            meta.copy(
              createTime = new Date(),
              startTime = new Date(),
              graphStatus = GraphStatus.RUNNING,
              id = atomicInteger.getAndIncrement(),
              reRunId = 0,
              reRunFlag = false,
              env = env ++ meta.env,
              latestTaskId = -1
            )
          } else {
            meta.copy(
              startTime = new Date(),
              graphStatus = GraphStatus.RUNNING,
              reRunId = meta.reRunId + 1,
              reRunFlag = false,
              env = env ++ meta.env,
              latestTaskId = -1
            )
          }

        metaHandler.onStart(start)
        start
      }

    val sink0 = Sink.foreach[RichGraphMeta](graphMetaHandler.onComplete)
    val graph = GraphDSL.create(source0, sink0)(_ -> _) { implicit b => (source, sink) =>
      val start = StartFlow(source)
      val end = EndFlow(sink)
      builder(GraphBuilder(start, end, metaHandler, b))
      ClosedShape
    }
    new Workflow(RunnableGraph.fromGraph(graph), metaHandler)
  }

  def create(builder: GraphBuilder => Unit)(implicit taskClient: TaskClient, graphMetaHandler: GraphMetaHandler): Workflow = {
    create(Map.empty, builder)(taskClient, graphMetaHandler)
  }

  def fromGraph(graphDefine: GraphDefine)(implicit taskClient: TaskClient, graphMetaHandler: GraphMetaHandler): Workflow = {
    create(graphDefine.env, implicit builder => {
      for ((prim, subs) <- graphDefine.edges) {
        val parent = graphDefine.nodes(prim)
        val children = subs.map(graphDefine.nodes)
        builder.buildEdges(parent, children:_*)
      }
      builder.createGraph()
    }
    )
  }

  def fromJson(json: String)(implicit taskClient: TaskClient, graphMetaHandler: GraphMetaHandler): Workflow = {
    fromGraph(DefaultJsonSerialization.deSerialize(json).asInstanceOf[GraphDefine])
  }
}