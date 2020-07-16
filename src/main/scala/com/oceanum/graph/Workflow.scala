package com.oceanum.graph

import akka.Done
import akka.actor.ActorSystem
import akka.stream.scaladsl.{RunnableGraph, SourceQueueWithComplete}
import com.oceanum.client.SchedulerClient

import scala.concurrent.Future
class Workflow(runnableGraph: RunnableGraph[(SourceQueueWithComplete[RichGraphMeta], Future[Done])]) {
  def run()(implicit client: SchedulerClient): (SourceQueueWithComplete[RichGraphMeta], Future[Done]) = {
    implicit val system: ActorSystem = client.system
    runnableGraph.run()
  }
}
