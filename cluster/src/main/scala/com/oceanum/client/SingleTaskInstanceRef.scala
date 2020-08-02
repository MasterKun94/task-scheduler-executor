package com.oceanum.client

import com.oceanum.common.RichTaskMeta
import com.oceanum.exec.{FAILED, State}

import scala.concurrent.{ExecutionContext, Future}

class SingleTaskInstanceRef(val instance: Future[TaskInstance])(implicit ec: ExecutionContext) {
  def kill(): Future[Unit] = instance.map(_.kill())

  def completeState: Future[State] = instance.flatMap(_.completeFuture)
}

object SingleTaskInstanceRef {
  import ExecutionContext.Implicits.global
  def failedInstance(taskMeta: RichTaskMeta): SingleTaskInstanceRef = new SingleTaskInstanceRef(null) {
    override def kill(): Future[Unit] = Future.successful(Unit)

    override def completeState: Future[State] = Future.successful(FAILED(taskMeta))
  }
}