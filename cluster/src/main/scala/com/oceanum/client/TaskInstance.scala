package com.oceanum.client

import akka.actor.ActorRef
import com.oceanum.exec.State
import com.oceanum.exec.State.FAILED
import com.oceanum.common.KillAction

import scala.concurrent.Future

/**
 * @author chenmingkun
 * @date 2020/6/18
 */
trait TaskInstance {
  def kill(): Unit

  def isCompleted: Boolean = completeFuture.isCompleted

  def completeFuture: Future[State]
}

object TaskInstance {
  def apply(executor: ActorRef, completeState: Future[State]): TaskInstance = new TaskInstance {
    override def kill(): Unit = executor ! KillAction
    override def completeFuture: Future[State] = completeState
  }
}
