package com.oceanum.graph

import akka.Done
import akka.stream.QueueOfferResult
import akka.stream.scaladsl.SourceQueueWithComplete
import com.oceanum.common.{GraphMeta, RichGraphMeta}

import scala.concurrent.Future

class WorkflowInstance(protected val queue: SourceQueueWithComplete[RichGraphMeta],
                       protected val future: Future[Done]) {
    def complete(): Future[Done] = {
      queue.complete()
      queue.watchCompletion()
    }

    def fail(e: Throwable): Unit = queue.fail(e)

    def offer(meta: GraphMeta): Future[QueueOfferResult] = queue.offer(RichGraphMeta(meta))

    def listenComplete: Future[Done] = future
  }

  object WorkflowInstance {
    def apply(queue: SourceQueueWithComplete[RichGraphMeta], future: Future[Done]): WorkflowInstance = new WorkflowInstance(queue, future)

    def unapply(arg: WorkflowInstance): Option[(SourceQueueWithComplete[RichGraphMeta], Future[Done])] = Some((arg.queue, arg.future))
  }