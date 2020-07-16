package com.oceanum.graph

import akka.actor.ActorSystem
import com.oceanum.client.TaskClient
class WorkflowRunner(runnableGraph: Graph) {
  def run()(implicit client: TaskClient): Mat = {
    implicit val system: ActorSystem = client.system
    runnableGraph.run()
  }
}
