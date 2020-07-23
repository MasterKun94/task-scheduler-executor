package com.oceanum.graph

import java.util.Date
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.stream.ClosedShape
import akka.stream.scaladsl.{GraphDSL, RunnableGraph, Sink, Source}
import com.oceanum.client.TaskClient
import com.oceanum.common.{Environment, GraphMeta, RichGraphMeta}
import com.oceanum.exec.State
import com.oceanum.graph.StreamFlows.{EndFlow, StartFlow}

import scala.util.{Failure, Success}
class WorkflowRunner(runnableGraph: Graph, graphMetaHandler: GraphMetaHandler) {
  def run()(implicit client: TaskClient): Workflow = {
    implicit val system: ActorSystem = client.system
    val (queue, future) = runnableGraph.run()
    val future0 = future.andThen {
      case Success(_) =>
        graphMetaHandler.close()
      case Failure(exception) =>
        exception.printStackTrace()
        graphMetaHandler.close()

    } (client.system.dispatcher)
    Workflow(queue, future0)
  }
}

object WorkflowRunner {
  def createGraph(builder: GraphBuilder => Unit)(implicit taskClient: TaskClient, graphMetaHandler: GraphMetaHandler): WorkflowRunner = {
    val metaHandler: GraphMetaHandler = new GraphMetaHandler {
      private val actor = taskClient.system.actorOf(Props(classOf[GraphMetaHandlerActor], graphMetaHandler))
      override def onRunning(richGraphMeta: GraphMeta, taskState: State): Unit = actor ! OnRunning(richGraphMeta, taskState)

      override def onComplete(richGraphMeta: GraphMeta): Unit = actor ! OnComplete(richGraphMeta)

      override def onStart(richGraphMeta: GraphMeta): Unit = actor ! OnStart(richGraphMeta)

      override def close(): Unit = actor ! PoisonPill
    }
    val atomicInteger = new AtomicInteger(0)
    val date = new Date()
    val source0 = Source
      .queue[RichGraphMeta](Environment.GRAPH_SOURCE_QUEUE_BUFFER_SIZE, Environment.GRAPH_SOURCE_QUEUE_OVERFLOW_STRATEGY)
      .map { meta =>
        val start = meta.copy(
          createTime = date,
          startTime = new Date(),
          graphStatus = GraphStatus.RUNNING,
          id = atomicInteger.getAndIncrement(),
          reRunFlag = false
        )
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
    new WorkflowRunner(RunnableGraph.fromGraph(graph), metaHandler)
  }
}