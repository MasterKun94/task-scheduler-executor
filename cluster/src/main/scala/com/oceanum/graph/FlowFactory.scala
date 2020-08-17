package com.oceanum.graph

import akka.stream.scaladsl.{Broadcast, Flow, Merge, Partition, ZipWithN}
import com.oceanum.client.{SingleTaskInstanceRef, StateHandler, Task, TaskClient}
import com.oceanum.common._
import com.oceanum.jdbc.expr.Evaluator
import com.oceanum.graph.StreamFlows._

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

object FlowFactory {

  def createStart(implicit builder: GraphBuilder): StartFlow = builder.startFlow

  def createEnd(implicit builder: GraphBuilder): EndFlow = builder.endFlow

  def createFlow(task: Task, parallelism: Int = 1)(implicit taskClient: TaskClient, builder: GraphBuilder): TaskFlow = {
    task.validate()
    createTask(parallelism)(_ => task)
  }

  def createTask(parallelism: Int)(taskFunc: GraphMeta => Task)(implicit taskClient: TaskClient, builder: GraphBuilder): TaskFlow = {
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
        case other: GraphStatus => Future.successful(graphMeta.copy(error = Option(new IllegalArgumentException("this should never happen, unexpected graph state: " + other))))
      }
    }
  }

  def createFork(parallel: Int)(implicit builder: GraphBuilder): ForkFlow = {
    ForkFlow(builder.dslBuilder.add(Broadcast(parallel)))
  }

  def createJoin(parallel: Int)(implicit builder: GraphBuilder): JoinFlow = {
    JoinFlow(builder.dslBuilder.add(ZipWithN[Message, Message](_.reduce((m1, m2) => m1.copy(m1.meta merge m2.meta)))(parallel)))
  }

  def createDecision(parallel: Int)(decide: GraphMeta => Int)(implicit builder: GraphBuilder): DecisionFlow = {
    DecisionFlow(builder.dslBuilder.add(Partition(parallel, message => decide(message.meta))))
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
    val meta2env = (meta: GraphMeta) => GraphContext(meta.env, graphMeta = RichGraphMeta(meta)).javaExprEnv
    val decide = expr
      .map(Evaluator.compile(_, cache = false))
      .map(expr => (meta: GraphMeta) => expr.execute(meta2env(meta)).asInstanceOf[Boolean])
    createDecision(decide)
  }

  def createConverge(parallel: Int)(implicit builder: GraphBuilder): ConvergeFlow = {
    ConvergeFlow(builder.dslBuilder.add(Merge(parallel)))
  }

  private def runOrReRun(task: Task)(implicit metadata: RichGraphMeta, schedulerClient: TaskClient, builder: GraphBuilder): Future[RichGraphMeta] = {
    metadata.rerunStrategy match {
      case RerunStrategy.NONE | RerunStrategy.RUN_ALL =>
        run(task)
      case RerunStrategy.RUN_ONLY_FAILED =>
        runIfNotSuccess(task)
      case RerunStrategy.RUN_ALL_AFTER_FAILED =>
        if (metadata.isReRun) {
          run(task)
        } else {
          runIfNotSuccess(task)
        }
    }
  }

  private def runIfNotSuccess(task: Task)(implicit metadata: RichGraphMeta, schedulerClient: TaskClient, builder: GraphBuilder): Future[RichGraphMeta] = {
    metadata.tasks.get(task.id) match {
      case Some(meta) =>
        meta.state match {
        case TaskStatus.KILL | TaskStatus.FAILED | TaskStatus.OFFLINE => run(task)
        case _: TaskStatus => Future.successful(metadata)
      }
      case None => run(task)
    }
  }

  private def run(task: Task)(implicit metadata: RichGraphMeta, schedulerClient: TaskClient, builder: GraphBuilder): Future[RichGraphMeta] = {
    import schedulerClient.system.dispatcher
    val promise = Promise[RichGraphMeta]()
    val stateHandler = StateHandler { state =>
      val taskMeta = state.metadata
      val graphMeta = metadata.addTask(taskMeta)
      builder.handler.onRunning(graphMeta, state)
    }
    val instance: SingleTaskInstanceRef = schedulerClient.execute(task, stateHandler)
    instance
      .completeState
      .onComplete {
        case Success(value) =>
          val taskMeta = value.metadata.copy(rerunId = metadata.rerunId)
          val graphMeta = metadata
            .addTask(taskMeta, isComplete = true)
            .copy(rerunFlag = true)
          promise.success(graphMeta)
        case Failure(e) =>
          e.printStackTrace()
          val taskMeta = task.env.taskMeta.asInstanceOf[RichTaskMeta].failure(task, e).copy(rerunId = metadata.rerunId)
          val graphMeta = metadata.addTask(taskMeta, isComplete = true)
          promise.success(graphMeta)
      }
    promise.future
  }

  def map(id: Int)(func: RichGraphMeta => RichGraphMeta): TaskFlow = {
    val flow = Flow[Message].map(message => message.copy(meta = func(message.meta)))
    TaskFlow(flow, id)
  }

  def mapAsync(parallelism: Int = 1, id: Int)(func: RichGraphMeta => Future[RichGraphMeta])(implicit taskClient: TaskClient): TaskFlow = {
    val flow = Flow[Message].mapAsync(parallelism) { message =>
      val meta = message.meta
      val promise = Promise[Message]()
      func(meta).onComplete {
        case Success(value) => promise.success(Message(value, message.instances))
        case Failure(e) => promise.success(Message(meta.copy(error = Option(e)), message.instances))
      } (taskClient.system.dispatcher)
      promise.future
    }
    TaskFlow(flow, id)
  }

}
