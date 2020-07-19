package com.oceanum.graph

import java.util.Date
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{PoisonPill, Props}
import akka.stream.ClosedShape
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, Partition, RunnableGraph, Sink, Source, ZipWithN}
import com.oceanum.client.{RichTaskMeta, StateHandler, Task, TaskClient}
import com.oceanum.cluster.exec.{FAILED, State}
import com.oceanum.common.Environment
import com.oceanum.common.Implicits.EnvHelper
import com.oceanum.expr.{Evaluator, JavaMap}
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
    mapAsync(parallelism) { implicit graphMeta =>
      val initialTask = taskFunc(graphMeta)
      val task = initialTask.addGraphMeta(graphMeta).copy(id = idValue)
      graphMeta.graphStatus match {
        case GraphStatus.RUNNING => runOrReRun(task)
        case GraphStatus.EXCEPTION => graphMeta.fallbackStrategy match {
          case FallbackStrategy.CONTINUE => runOrReRun(task)
          case FallbackStrategy.SHUTDOWN => Future.successful(graphMeta.updateGraphStatus(GraphStatus.FAILED))
        }
        case GraphStatus.FAILED | GraphStatus.KILLED => Future.successful(graphMeta)
        case other: GraphStatus.value => Future.successful(graphMeta.error = new IllegalArgumentException("this should never happen, unexpected graph state: " + other))
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

  def createDecision(decide: Array[GraphMeta[_] => Boolean])(implicit builder: GraphBuilder): Decision = {
    val parallel = decide.length + 1
    val func: GraphMeta[_] => Int = meta => decide.zipWithIndex.find(_._1(meta)).map(_._2) match {
      case Some(int) => int
      case None => parallel - 1
    }
    createDecision(parallel)(func)
  }

  def createDecision(expr: Array[String])(implicit builder: GraphBuilder): Decision = {
    val parallel = expr.length + 1
    val func: GraphMeta[_] => Int = meta => {
      val env: JavaMap[String, AnyRef] = meta.env.combineGraph(meta.asInstanceOf[RichGraphMeta]).toJava
      val booFunc: String => Boolean = str => Evaluator.rawExecute(str, env) match {
        case s: String => s.toBoolean
        case b: Boolean => b
        case other => other.toString.toBoolean
      }
      expr.zipWithIndex.find(t => booFunc(t._1)).map(_._2) match {
        case Some(int) => int
        case None => parallel - 1
      }
    }
    createDecision(parallel)(func)
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
    val atomicInteger = new AtomicInteger(0)
    val date = new Date()
    val source0 = Source
      .queue[RichGraphMeta](Environment.GRAPH_SOURCE_QUEUE_BUFFER_SIZE, Environment.GRAPH_SOURCE_QUEUE_OVERFLOW_STRATEGY)
      .map { meta =>
        val start = meta.copy(
          _.createTime = date,
          _.startTime = new Date(),
          _.graphStatus = GraphStatus.RUNNING,
          _.id = atomicInteger.getAndIncrement(),
          _.reRunFlag = false
        )
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
      case Some(meta) =>
        meta.state match {
        case State.KILL | State.FAILED | State.OFFLINE => run(task)
        case _: State.value => Future.successful(metadata)
      }
      case None => run(task)
    }
  }

  private def run(task: Task)(implicit metadata: RichGraphMeta, schedulerClient: TaskClient, builder: GraphBuilder): Future[RichGraphMeta] = {
    import schedulerClient.system.dispatcher
    val promise = Promise[RichGraphMeta]()
    val stateHandler = StateHandler { state =>
      val graphMeta = metadata.operators_+(state)
      builder.handler.onRunning(graphMeta, state)
    }
    schedulerClient.execute(task, stateHandler)
      .completeState.onComplete {
      case Success(value) =>
        val meta = metadata.operators_+(value).reRunFlag = true
        promise.success(meta)
      case Failure(e) =>
        e.printStackTrace()
        val meta = metadata.operators_+(FAILED(RichTaskMeta().failure(task, e)))
        promise.success(meta)
    }
    promise.future
  }
}
