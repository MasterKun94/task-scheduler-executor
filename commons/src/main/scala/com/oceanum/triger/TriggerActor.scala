package com.oceanum.triger

import akka.actor.Actor

class TriggerActor extends Actor {
  override def receive: Receive = {
    case action: TriggerAction => action.action()
  }
}
