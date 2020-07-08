package com.oceanum.graph

import akka.NotUsed
import akka.stream.{UniformFanInShape, UniformFanOutShape}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, Partition, ZipWithN}
import com.oceanum.client.{Metadata, SchedulerClient, Task}
import com.oceanum.cluster.exec.State.{FAILED, KILL, SUCCESS}
import com.oceanum.common.Environment

import scala.concurrent.{ExecutionContext, Promise}
import scala.util.{Failure, Success}

class FlowFactory {
  implicit val sc: ExecutionContext = Environment.GLOBAL_EXECUTOR
  def create(task: Task)(implicit schedulerClient: SchedulerClient): Flow[Metadata, Metadata, NotUsed] = {
    Flow[Metadata].mapAsync(1) { metadata =>
      val promise = Promise[Metadata]()
      val instanceFuture = schedulerClient.execute(task)
      instanceFuture.onComplete {
        case Success(taskInstance) =>
          taskInstance.onComplete { // TODO
            case SUCCESS(meta) => promise.success(metadata ++ meta)
            case FAILED(meta) => promise.success(metadata ++ meta)
            case KILL(meta) => promise.success(metadata ++ meta)
          }
        case Failure(e) =>
          e.printStackTrace()
          promise.success(metadata ++ Metadata("error" -> e))
      }
      promise.future
    }
  }

  def broadcast(parallel: Int)(implicit builder: GraphDSL.Builder[NotUsed]): UniformFanOutShape[Metadata, Metadata] = {
    builder.add(Broadcast(parallel))
  }

  def zip(parallel: Int)(implicit builder: GraphDSL.Builder[NotUsed]): UniformFanInShape[Metadata, Metadata] = {
    builder.add(ZipWithN[Metadata, Metadata](_.reduce(_ ++ _))(parallel))
  }

  def partition(parallel: Int)(partitioner: Metadata => Int)(implicit builder: GraphDSL.Builder[NotUsed]): UniformFanOutShape[Metadata, Metadata] = {
    builder.add(Partition(parallel, partitioner))
  }

  def merge(parallel: Int)(implicit builder: GraphDSL.Builder[NotUsed]): UniformFanInShape[Metadata, Metadata] = {
    builder.add(Merge(parallel))
  }
}
