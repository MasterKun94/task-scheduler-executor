package com.oceanum.graph

import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, Partition, ZipWithN}
import com.oceanum.client.{SchedulerClient, Task, TaskMeta}
import com.oceanum.cluster.exec.FAILED
import com.oceanum.common.Environment
import com.oceanum.graph.Operator._

import scala.concurrent.{ExecutionContext, Promise}
import scala.util.{Failure, Success}

object FlowFactory {
  implicit val sc: ExecutionContext = Environment.CLUSTER_NODE_TASK_INIT_EXECUTOR


  def flow(task: Task)(implicit schedulerClient: SchedulerClient): TaskFlow = {
    val flow = Flow[GraphMeta].mapAsync(task.parallelism) { metadata =>
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
    TaskFlow(flow)
  }

  def fork[T](parallel: Int)(implicit builder: GraphDSL.Builder[T]): Fork = {
    Fork(builder.add(Broadcast(parallel)))
  }

  def join[T](parallel: Int)(implicit builder: GraphDSL.Builder[T]): Join = {
    Join(builder.add(ZipWithN[GraphMeta, GraphMeta](_.reduce(_ merge _))(parallel)))
  }

  def decision[T](parallel: Int)(partitioner: GraphMeta => Int)(implicit builder: GraphDSL.Builder[T]): Decision = {
    Decision(builder.add(Partition(parallel, partitioner)))
  }

  def converge[T](parallel: Int)(implicit builder: GraphDSL.Builder[T]): Converge = {
    Converge(builder.add(Merge(parallel)))
  }
}
