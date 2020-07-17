package com.oceanum.graph

import akka.actor.ActorSystem
import com.oceanum.client.TaskClient
class WorkflowRunner(runnableGraph: Graph, graphMetaHandler: GraphMetaHandler) {
  def run()(implicit client: TaskClient): Workflow = {
    implicit val system: ActorSystem = client.system
    val (queue, future) = runnableGraph.run()
    val future0 = future.andThen {
      case _ => graphMetaHandler.close()
    } (client.system.dispatcher)
    Workflow(queue, future0)
  }
}
