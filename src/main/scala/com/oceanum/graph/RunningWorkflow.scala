package com.oceanum.graph

import akka.Done
import akka.actor.ActorRef
import akka.actor.Status.Success
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.{ExecutionContext, Future}

class RunningWorkflow(actorRef: ActorRef, val doneFlag: Future[Done]) {
  def receive(meta: RichGraphMeta)(implicit timeout: Timeout, ec: ExecutionContext): Future[Unit] = actorRef.ask(meta)map(_.asInstanceOf[Unit])

  def stop(): Unit = actorRef ! Success()
}
