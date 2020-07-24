package com.oceanum.graph

import akka.stream.scaladsl.{Broadcast, Flow, Merge, Partition, ZipWithN}
import com.oceanum.client.{StateHandler, Task, TaskClient}
import com.oceanum.common.{ExprContext, GraphMeta, RichGraphMeta}
import com.oceanum.exec.{FAILED, State}
import com.oceanum.expr.Evaluator
import com.oceanum.graph.StreamFlows._

import scala.concurrent.{Future, Promise}
import scala.language.implicitConversions
import scala.util.{Failure, Success}

object FlowFactory {

  def createStart(implicit builder: GraphBuilder): StartFlow = builder.startFlow

  def createEnd(implicit builder: GraphBuilder): EndFlow = builder.endFlow

  def createFlow(task: Task, parallelism: Int = 1)(implicit taskClient: TaskClient, builder: GraphBuilder): TaskFlow = {
    task.validate()
    createOperator(parallelism)(_ => task)
  }

  def createOperator(parallelism: Int)(taskFunc: GraphMeta => Task)(implicit taskClient: TaskClient, builder: GraphBuilder): TaskFlow = {
    val idValue = builder.idValue
    mapAsync(parallelism, idValue) { implicit graphMeta =>
      val initialTask = taskFunc(graphMeta)
      val task = initialTask.addGraphMeta(graphMeta).copy(id = idValue)
      graphMeta.graphStatus match {
        case GraphStatus.RUNNING => runOrReRun(task)
        case GraphStatus.EXCEPTION => graphMeta.fallbackStrategy match {
          case FallbackStrategy.CONTINUE => runOrReRun(task)
          case FallbackStrategy.SHUTDOWN => Future.successful(graphMeta.updateGraphStatus(GraphStatus.FAILED))
        }
        case GraphStatus.FAILED | GraphStatus.KILLED => Future.successful(graphMeta)
        case other: GraphStatus.value => Future.successful(graphMeta.copy(error = new IllegalArgumentException("this should never happen, unexpected graph state: " + other)))
      }
    }
  }

  def createFork(parallel: Int)(implicit builder: GraphBuilder): ForkFlow = {
    ForkFlow(builder.dslBuilder.add(Broadcast(parallel)))
  }

  def createJoin(parallel: Int)(implicit builder: GraphBuilder): JoinFlow = {
    JoinFlow(builder.dslBuilder.add(ZipWithN[RichGraphMeta, RichGraphMeta](_.reduce(_ merge _))(parallel)))
  }

  def createDecision(parallel: Int)(decide: GraphMeta => Int)(implicit builder: GraphBuilder): DecisionFlow = {
    DecisionFlow(builder.dslBuilder.add(Partition(parallel, decide)))
  }

  def createDecision(decide: Array[GraphMeta => Boolean])(implicit builder: GraphBuilder): DecisionFlow = {
    val parallel = decide.length + 1
    val func: GraphMeta => Int = meta => decide.zipWithIndex.find(_._1(meta)).map(_._2) match {
      case Some(int) => int
      case None => parallel - 1
    }
    createDecision(parallel)(func)
  }

  def createDecision(expr: Array[String])(implicit builder: GraphBuilder): DecisionFlow = {
    val meta2env = (meta: GraphMeta) => (ExprContext(meta.env, graphMeta = meta.asInstanceOf[RichGraphMeta])).javaExprEnv
    val decide = expr
      .map(Evaluator.compile(_, cache = false))
      .map(expr => (meta: GraphMeta) => expr.execute(meta2env(meta)).asInstanceOf[Boolean])
    createDecision(decide)
  }

  def createConverge(parallel: Int)(implicit builder: GraphBuilder): ConvergeFlow = {
    ConvergeFlow(builder.dslBuilder.add(Merge(parallel)))
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
      val graphMeta = metadata.addOperators(state)
      builder.handler.onRunning(graphMeta, state)
    }
    schedulerClient.execute(task, stateHandler)
      .completeState.onComplete {
      case Success(value) =>
        val meta = metadata.addOperators(value).copy(reRunFlag = true)
        promise.success(meta)
      case Failure(e) =>
        e.printStackTrace()
        val meta = metadata.addOperators(FAILED(task.env.taskMeta.failure(task, e)))
        promise.success(meta)
    }
    promise.future
  }

  def map(id: Int)(func: RichGraphMeta => RichGraphMeta): TaskFlow = {
    val flow = Flow[RichGraphMeta].map(func)
    TaskFlow(flow, id)
  }

  def mapAsync(parallelism: Int = 1, id: Int)(func: RichGraphMeta => Future[RichGraphMeta])(implicit taskClient: TaskClient): TaskFlow = {
    val flow = Flow[RichGraphMeta].mapAsync(parallelism) { meta =>
      val promise = Promise[RichGraphMeta]()
      func(meta).onComplete {
        case Success(value) => promise.success(value)
        case Failure(e) => promise.success(meta.copy(error = e))
      } (taskClient.system.dispatcher)
      promise.future
    }
    TaskFlow(flow, id)
  }

}
