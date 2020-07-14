package com.oceanum.graph

import akka.NotUsed
import akka.stream.{UniformFanInShape, UniformFanOutShape}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, Partition, ZipWithN}
import com.oceanum.client.{TaskMeta, SchedulerClient, Task}
import com.oceanum.cluster.exec.State.{FAILED, KILL, SUCCESS}
import com.oceanum.common.Environment

import scala.concurrent.{ExecutionContext, Promise}
import scala.util.{Failure, Success}

object FlowFactory {
  implicit val sc: ExecutionContext = Environment.CLUSTER_NODE_TASK_INIT_EXECUTOR
  def create(task: Task)(implicit schedulerClient: SchedulerClient): Flow[GraphMeta, GraphMeta, NotUsed] = {
    Flow[GraphMeta].mapAsync(task.parallelism) { metadata =>
      val promise = Promise[GraphMeta]()
      schedulerClient.execute(task)
        .completeState.onComplete {
        case Success(value) =>
          val meta = metadata.operatorStatus_+(value)
          promise.success(meta)
        case Failure(e) =>
          val meta = metadata.operatorStatus_+(FAILED(TaskMeta().withTask(task).error = e))
          promise.success(meta)
      }
      promise.future
    }
  }

  def broadcast[T](parallel: Int)(implicit builder: GraphDSL.Builder[T]): UniformFanOutShape[GraphMeta, GraphMeta] = {
    builder.add(Broadcast(parallel))
  }

  def zip[T](parallel: Int)(implicit builder: GraphDSL.Builder[T]): UniformFanInShape[GraphMeta, GraphMeta] = {
    builder.add(ZipWithN[GraphMeta, GraphMeta](_.reduce(_ merge _))(parallel))
  }

  def partition[T](parallel: Int)(partitioner: GraphMeta => Int)(implicit builder: GraphDSL.Builder[T]): UniformFanOutShape[GraphMeta, GraphMeta] = {
    builder.add(Partition(parallel, partitioner))
  }

  def merge[T](parallel: Int)(implicit builder: GraphDSL.Builder[T]): UniformFanInShape[GraphMeta, GraphMeta] = {
    builder.add(Merge(parallel))
  }
}
