package com.oceanum.graph

import akka.Done
import akka.stream.QueueOfferResult
import akka.stream.scaladsl.SourceQueueWithComplete
import com.oceanum.client.SingleTaskInstanceRef
import com.oceanum.common.{GraphMeta, RichGraphMeta}

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future
import com.oceanum.common.Environment.NONE_BLOCKING_EXECUTION_CONTEXT

class WorkflowInstance(protected val queue: SourceQueueWithComplete[Message],
                       protected val future: Future[Done]) {
  def complete(): Future[Done] = {
    queue.complete()
    queue.watchCompletion()
  }

  def fail(e: Throwable): Unit = queue.fail(e)

  def offer(meta: GraphMeta): Future[(QueueOfferResult, GraphHook)] = {
    val map: TrieMap[SingleTaskInstanceRef, Unit] = TrieMap()
    val future = queue.offer(Message(RichGraphMeta(meta), map))
    future.map((_, GraphHook(map)))
  }

  def listenComplete: Future[Done] = future
}

object WorkflowInstance {
  def apply(queue: SourceQueueWithComplete[Message], future: Future[Done]): WorkflowInstance = new WorkflowInstance(queue, future)

  def unapply(arg: WorkflowInstance): Option[(SourceQueueWithComplete[Message], Future[Done])] = Some((arg.queue, arg.future))
}