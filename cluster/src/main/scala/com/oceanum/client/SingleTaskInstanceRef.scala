package com.oceanum.client

import com.oceanum.exec.State

import scala.concurrent.{ExecutionContext, Future}

class SingleTaskInstanceRef(val instance: Future[TaskInstance])(implicit ec: ExecutionContext) {
  def kill(): Future[Unit] = instance.map(_.kill())

  def completeState: Future[State] = instance.flatMap(_.completeFuture)
}