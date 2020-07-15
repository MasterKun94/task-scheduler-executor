package com.oceanum.graph

import akka.Done
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.scaladsl.RunnableGraph
import com.oceanum.client.SchedulerClient

import scala.concurrent.Future

class Workflow(runnableGraph: RunnableGraph[(ActorRef, Future[Done])]) {
  def run()(implicit client: SchedulerClient): (ActorRef, Future[Done]) = {
    implicit val system: ActorSystem = client.system
    runnableGraph.run()
  }
}
