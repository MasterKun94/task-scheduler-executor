package com.oceanum.pluggable

import akka.actor.{ActorRef, ActorSystem}

object Env {
  val system: ActorSystem = ???

  @SerialVersionUID(1L)
  case class UpdateState(info: Map[String, String])
  @SerialVersionUID(1L)
  case object Kill
}
