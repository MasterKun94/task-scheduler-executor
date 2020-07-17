package com.oceanum.graph

import akka.Done
import akka.stream.QueueOfferResult
import akka.stream.scaladsl.SourceQueueWithComplete

import scala.concurrent.Future

class Workflow(protected val queue: SourceQueueWithComplete[RichGraphMeta],
                 protected val future: Future[Done]) {
    def complete(): Future[Done] = {
      queue.complete()
      queue.watchCompletion()
    }

    def fail(e: Throwable): Unit = queue.fail(e)

    def offer(meta: RichGraphMeta): Future[QueueOfferResult] = queue.offer(meta)

    def listenComplete: Future[Done] = future
  }

  object Workflow {
    def apply(queue: SourceQueueWithComplete[RichGraphMeta], future: Future[Done]): Workflow = new Workflow(queue, future)

    def unapply(arg: Workflow): Option[(SourceQueueWithComplete[RichGraphMeta], Future[Done])] = Some((arg.queue, arg.future))
  }