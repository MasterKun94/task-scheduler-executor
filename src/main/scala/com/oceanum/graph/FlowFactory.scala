package com.oceanum.graph

import akka.actor.ActorRef
import akka.{Done, NotUsed}
import akka.stream.{ClosedShape, OverflowStrategy}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, Partition, RunnableGraph, Sink, Source, SourceQueueWithComplete, ZipWithN}
import com.oceanum.client.{RichTaskMeta, SchedulerClient, Task}
import com.oceanum.cluster.exec.FAILED
import com.oceanum.graph.Operator._

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.language.implicitConversions
import scala.util.{Failure, Success}

object FlowFactory {
  case class GraphBuilder(start: Start, end: End, implicit protected[graph] val dslBuilder: GraphDSL.Builder[(ActorRef, Future[Done])])
  implicit private def toRich(meta: GraphMeta[_]): RichGraphMeta = meta.asInstanceOf[RichGraphMeta]

  def map(func: GraphMeta[_] => GraphMeta[_]): TaskFlow = {
    val flow = Flow[RichGraphMeta].map(func).map(toRich)
    TaskFlow(flow)
  }

  def mapAsync(parallelism: Int)(func: GraphMeta[_] => Future[GraphMeta[_]])(implicit ec: ExecutionContext): TaskFlow = {
    val flow = Flow[GraphMeta[_]].mapAsync(parallelism) { meta: GraphMeta[_] =>
      val promise = Promise[RichGraphMeta]()
      func(meta).onComplete {
        case Success(value) => promise.success(value)
        case Failure(e) => promise.success(toRich(meta).error = e)
      }
      promise.future
    }
    TaskFlow(flow)
  }

  def createFlow(task: Task)(implicit schedulerClient: SchedulerClient): TaskFlow = {
    createFlow()(_ => task)
  }

  def createFlow(parallelism: Int = 1)(taskFunc: GraphMeta[_] => Task)(implicit schedulerClient: SchedulerClient): TaskFlow = {
    val flow = Flow[RichGraphMeta].mapAsync(parallelism) { implicit metadata =>
      metadata.graphStatus match {
        case GraphStatus.RUNNING => create(taskFunc)

        case GraphStatus.EXCEPTION => metadata.fallbackStrategy match {
            case FallbackStrategy.CONTINUE => create(taskFunc)

            case FallbackStrategy.SHUTDOWN => Future.successful(metadata.updateGraphStatus(GraphStatus.FAILED))
          }

        case GraphStatus.FAILED | GraphStatus.KILLED =>
          Future.successful(metadata)

        case state: GraphStatus.value =>
          Future.successful(metadata.error = new IllegalArgumentException("this should never happen, unexpected graph state: " + state))
      }
    }
    TaskFlow(flow)
  }

  def create(taskFunc: GraphMeta[_] => Task)(implicit metadata: RichGraphMeta, schedulerClient: SchedulerClient): Future[RichGraphMeta] = {
    import schedulerClient.system.dispatcher
    val promise = Promise[RichGraphMeta]()
    val task = taskFunc(metadata)
    schedulerClient.execute(task)
      .completeState.onComplete {
      case Success(value) =>
        val meta = metadata.operators_+(value)
        promise.success(meta)
      case Failure(e) =>
        e.printStackTrace()
        val meta = metadata.operators_+(FAILED(RichTaskMeta().withTask(task).error = e))
        promise.success(meta)
    }
    promise.future
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

  def createGraph(builder: GraphBuilder => Unit): Workflow = {
    val source0: Source[RichGraphMeta, ActorRef] = Source.actorRefWithAck[RichGraphMeta](Unit).map(_.start)
    val sink0 = Sink.foreach[GraphMeta[_]]{ metadata => {
      println(metadata.graphStatus)
      println(metadata.operators.mkString("\r\n"))
    }}
    val graph = RunnableGraph fromGraph GraphDSL.create(source0, sink0)((_, _)) { implicit b =>(source, sink) =>
      val start = Start(source)
      val end = End(sink)
      builder(GraphBuilder(start, end, b))
      ClosedShape
    }
    new Workflow(graph)
  }
}
