package com.oceanum.client

import akka.actor.ActorRef
import com.oceanum.cluster.exec.State
import com.oceanum.cluster.exec.State.FAILED
import com.oceanum.common.KillAction

import scala.concurrent.Future

/**
 * @author chenmingkun
 * @date 2020/6/18
 */
trait TaskInstance {
  def kill(): Unit

  def completeFuture: Future[State]
}

object TaskInstance {
  def apply(executor: ActorRef, completeState: Future[State]): TaskInstance = new TaskInstance {
    override def kill(): Unit = executor ! KillAction
    override def completeFuture: Future[State] = completeState
  }

  def apply(e: Throwable, metadata: TaskMeta): TaskInstance = new TaskInstance {
    override def kill(): Unit = Unit

    override def completeFuture: Future[State] = Future.successful {
      FAILED(metadata
        .withFunc(_.error = e)
        .withFunc(_.message = e.getMessage))
    }
  }
}
