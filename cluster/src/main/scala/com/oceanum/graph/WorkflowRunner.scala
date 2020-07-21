package com.oceanum.graph

import akka.actor.ActorSystem
import com.oceanum.client.TaskClient

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
