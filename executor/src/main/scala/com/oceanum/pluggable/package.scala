package com.oceanum

import akka.actor.{ActorSelection, ActorSystem}

package object pluggable {

  val system: ActorSystem = ???
  val primEndpoint: ActorSelection = ???
}
