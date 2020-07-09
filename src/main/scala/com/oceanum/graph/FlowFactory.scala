package com.oceanum.graph

import akka.NotUsed
import akka.stream.{UniformFanInShape, UniformFanOutShape}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, Partition, ZipWithN}
import com.oceanum.client.{TaskMeta, SchedulerClient, Task}
import com.oceanum.cluster.exec.State.{FAILED, KILL, SUCCESS}
import com.oceanum.common.Environment

import scala.concurrent.{ExecutionContext, Promise}
import scala.util.{Failure, Success}

class FlowFactory {
  implicit val sc: ExecutionContext = Environment.GLOBAL_EXECUTOR
  def create(task: Task)(implicit schedulerClient: SchedulerClient): Flow[TaskMeta, TaskMeta, NotUsed] = {
//    Flow[TaskMeta].mapAsync(1) { metadata =>
//      val promise = Promise[TaskMeta]()
//      val instanceFuture = schedulerClient.execute(task)
//      instanceFuture.flatMap(_.completeFuture.onComplete { // TODO
//            case Success(state) => state match {
//              case SUCCESS(meta) => promise.success(metadata ++ meta)
//              case FAILED(meta) => promise.success(metadata ++ meta)
//              case KILL(meta) => promise.success(metadata ++ meta)
//            }
//            case Failure(e) =>
//              e.printStackTrace()
//          })
      null

//    }
  }

  def broadcast(parallel: Int)(implicit builder: GraphDSL.Builder[NotUsed]): UniformFanOutShape[TaskMeta, TaskMeta] = {
    builder.add(Broadcast(parallel))
  }

  def zip(parallel: Int)(implicit builder: GraphDSL.Builder[NotUsed]): UniformFanInShape[TaskMeta, TaskMeta] = {
    builder.add(ZipWithN[TaskMeta, TaskMeta](_.reduce(_ ++ _))(parallel))
  }

  def partition(parallel: Int)(partitioner: TaskMeta => Int)(implicit builder: GraphDSL.Builder[NotUsed]): UniformFanOutShape[TaskMeta, TaskMeta] = {
    builder.add(Partition(parallel, partitioner))
  }

  def merge(parallel: Int)(implicit builder: GraphDSL.Builder[NotUsed]): UniformFanInShape[TaskMeta, TaskMeta] = {
    builder.add(Merge(parallel))
  }
}
