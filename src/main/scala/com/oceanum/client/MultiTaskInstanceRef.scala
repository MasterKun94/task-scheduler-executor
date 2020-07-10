package com.oceanum.client

import com.oceanum.cluster.exec.State

import scala.concurrent.{ExecutionContext, Future}

class MultiTaskInstanceRef(val instances: Future[Seq[TaskInstance]])(implicit ex: ExecutionContext) {

  def killAll(): Future[Unit] = instances.map(_.map(_.kill()).reduce((_, _) => Unit))

  def completeState: Future[Seq[State]] = instances.flatMap(seq => {
    val futures: Seq[Future[State]] = seq.map(_.completeFuture)
    futures.foldRight[Future[Seq[State]]](Future(Seq())) { (fStat, fSeq) =>
      fSeq.flatMap(seq => fStat.map(seq :+ _))
    }
  })

  def size: Future[Int] = instances.map(_.size)
}
