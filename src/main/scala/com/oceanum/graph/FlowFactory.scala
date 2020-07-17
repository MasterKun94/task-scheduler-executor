package com.oceanum.graph

import java.util.concurrent.atomic.AtomicInteger

import akka.Done
import akka.actor.{PoisonPill, Props}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, Partition, RunnableGraph, Sink, Source, SourceQueueWithComplete, ZipWithN}
import akka.stream.{ClosedShape, OverflowStrategy}
import com.oceanum.client.{RichTaskMeta, StateHandler, Task, TaskClient}
import com.oceanum.cluster.exec.{FAILED, State}
import com.oceanum.common.Environment
import com.oceanum.graph.Operator._

import scala.concurrent.{Future, Promise}
import scala.language.implicitConversions
import scala.util.{Failure, Success}

object FlowFactory {

  def map(func: RichGraphMeta => RichGraphMeta): TaskFlow = {
    val flow = Flow[RichGraphMeta].map(func)
    TaskFlow(flow)
  }

  def mapAsync(parallelism: Int = 1)(func: RichGraphMeta => Future[RichGraphMeta])(implicit taskClient: TaskClient): TaskFlow = {
    val flow = Flow[RichGraphMeta].mapAsync(parallelism) { meta =>
      val promise = Promise[RichGraphMeta]()
      func(meta).onComplete {
        case Success(value) => promise.success(value)
        case Failure(e) => promise.success(meta.error = e)
      } (taskClient.system.dispatcher)
      promise.future
    }
    TaskFlow(flow)
  }

  def createFlow(task: Task, parallelism: Int = 1)(implicit taskClient: TaskClient, builder: GraphBuilder): TaskFlow = {
    createFlow(parallelism)(_ => task)
  }

  def createFlow(parallelism: Int)(taskFunc: GraphMeta[_] => Task)(implicit taskClient: TaskClient, builder: GraphBuilder): TaskFlow = {
    val idValue = builder.idValue
    mapAsync(parallelism) { implicit metadata =>
      val initialTask = taskFunc(metadata)
      val task = initialTask.copy(
        id = idValue,
        env = metadata.env ++ initialTask.env
      )
      metadata.graphStatus match {
        case GraphStatus.RUNNING => runOrReRun(task)
        case GraphStatus.EXCEPTION => metadata.fallbackStrategy match {
          case FallbackStrategy.CONTINUE => runOrReRun(task)
          case FallbackStrategy.SHUTDOWN => Future.successful(metadata.updateGraphStatus(GraphStatus.FAILED))
        }
        case GraphStatus.FAILED | GraphStatus.KILLED => Future.successful(metadata)
        case other: GraphStatus.value => Future.successful(metadata.error = new IllegalArgumentException("this should never happen, unexpected graph state: " + other))
      }
    }
  }

  def createFork(parallel: Int)(implicit builder: GraphBuilder): Fork = {
    Fork(builder.dslBuilder.add(Broadcast(parallel)))
  }

  def createJoin(parallel: Int)(implicit builder: GraphBuilder): Join = {
    Join(builder.dslBuilder.add(ZipWithN[RichGraphMeta, RichGraphMeta](_.reduce(_ merge _))(parallel)))
  }

  def createDecision(parallel: Int)(decide: GraphMeta[_] => Int)(implicit builder: GraphBuilder): Decision = {
    Decision(builder.dslBuilder.add(Partition(parallel, decide)))
  }

  def createConverge(parallel: Int)(implicit builder: GraphBuilder): Converge = {
    Converge(builder.dslBuilder.add(Merge(parallel)))
  }

  def createGraph(builder: GraphBuilder => Unit)(implicit taskClient: TaskClient, graphMetaHandler: GraphMetaHandler): WorkflowRunner = {
    val metaHandler: GraphMetaHandler = new GraphMetaHandler {
      private val actor = taskClient.system.actorOf(Props(classOf[GraphMetaHandlerActor], graphMetaHandler))
      override def onRunning(richGraphMeta: RichGraphMeta, taskState: State): Unit = actor ! OnRunning(richGraphMeta, taskState)

      override def onComplete(richGraphMeta: RichGraphMeta): Unit = actor ! OnComplete(richGraphMeta)

      override def onStart(richGraphMeta: RichGraphMeta): Unit = actor ! OnStart(richGraphMeta)

      override def close(): Unit = actor ! PoisonPill
    }

    val source0 = Source
      .queue[RichGraphMeta](Environment.GRAPH_SOURCE_QUEUE_BUFFER_SIZE, Environment.GRAPH_SOURCE_QUEUE_OVERFLOW_STRATEGY)
      .map { meta =>
        val start = meta.start
        metaHandler.onStart(start)
        start
      }

    val sink0 = Sink.foreach[RichGraphMeta](graphMetaHandler.onComplete)
    val graph = GraphDSL.create(source0, sink0)(_ -> _) { implicit b => (source, sink) =>
      val start = Start(source)
      val end = End(sink)
      builder(GraphBuilder(start, end, b, metaHandler))
      ClosedShape
    }
    new WorkflowRunner(RunnableGraph.fromGraph(graph), metaHandler)
  }

  private def runOrReRun(task: Task)(implicit metadata: RichGraphMeta, schedulerClient: TaskClient, builder: GraphBuilder): Future[RichGraphMeta] = {
    metadata.reRunStrategy match {
      case ReRunStrategy.NONE | ReRunStrategy.RUN_ALL =>
        run(task)
      case ReRunStrategy.RUN_ONLY_FAILED =>
        runIfNotSuccess(task)
      case ReRunStrategy.RUN_ALL_AFTER_FAILED =>
        if (metadata.reRunFlag) {
          run(task)
        } else {
          runIfNotSuccess(task)
        }
    }
  }

  private def runIfNotSuccess(task: Task)(implicit metadata: RichGraphMeta, schedulerClient: TaskClient, builder: GraphBuilder): Future[RichGraphMeta] = {
    metadata.operators.get(task.id) match {
      case Some(meta) => meta.state match {
        case State.KILL | State.FAILED | State.OFFLINE => run(task)
        case _: State.value => Future.successful(metadata)
      }
      case None => run(task)
    }
  }

  private def run(task: Task)(implicit metadata: RichGraphMeta, schedulerClient: TaskClient, builder: GraphBuilder): Future[RichGraphMeta] = {
    val m = metadata.reRunFlag = true
    import schedulerClient.system.dispatcher
    val promise = Promise[RichGraphMeta]()
    val stateHandler = StateHandler { state =>
      val graphMeta = metadata.operators_+(state)
      builder.handler.onRunning(graphMeta, state)
    }
    schedulerClient.execute(task, stateHandler)
      .completeState.onComplete {
      case Success(value) =>
        val meta = m.operators_+(value)
        promise.success(meta)
      case Failure(e) =>
        e.printStackTrace()
        val meta = m.operators_+(FAILED(RichTaskMeta().withTask(task).error = e))
        promise.success(meta)
    }
    promise.future
  }
}
