package com.oceanum.pluggable

import java.util.concurrent.Executors

import akka.actor.{Actor, ActorSelection}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

class PluggableSubEndPoint(actor: ActorSelection, executor: PluggableExecutor) extends Actor {
  private implicit val ex: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())

  override def preStart(): Unit = {
    Future(executor.startRun()).onComplete {
      case Success(value) => self ! value

      case Failure(exception) => self ! TaskFailed(exception)
    }
  }

  override def postStop(): Unit = {

  }

  override def receive: Receive = {
    case CheckPluggableState => // send state

    case Kill => // kill

    case msg: TaskFailed => actor ! msg

    case msg: TaskSuccess => actor ! msg

    case msg: TaskKilled => actor ! msg
  }
}