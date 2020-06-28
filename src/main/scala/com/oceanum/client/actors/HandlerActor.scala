package com.oceanum.client.actors

import akka.actor.Actor
import com.oceanum.common.{Ping, Pong}

/**
 * @author chenmingkun
 * @date 2020/6/28
 */
class HandlerActor[T](handler: Actor.Receive) extends Actor {
  override def receive: Receive = handler orElse {
    case Ping => sender() ! Pong
  }
}
