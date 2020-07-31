package com.oceanum.pluggable

import akka.actor.{ActorSelection, ActorSystem}

object Env {
  val system: ActorSystem = ???
  val primEndpoint: ActorSelection = ???
}
