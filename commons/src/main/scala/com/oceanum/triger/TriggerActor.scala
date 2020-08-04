package com.oceanum.triger

import akka.actor.Actor
import com.typesafe.akka.extension.quartz.MessageWithFireTime

class TriggerActor extends Actor {
  override def receive: Receive = {
    case MessageWithFireTime(action, time) =>
      action.asInstanceOf[TriggerAction].action(time)
  }
}
