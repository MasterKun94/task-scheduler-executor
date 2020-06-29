package com.oceanum.client.actors

import akka.actor.{Actor, ActorRef}
import com.oceanum.common.{Ping, Pong}

/**
 * @author chenmingkun
 * @date 2020/6/28
 */
class HandlerActor(handler: ActorRef => Actor.Receive) extends Actor {
  override def receive: Receive = handler(self) orElse {
    case Ping => sender() ! Pong
  }
}
