package com.oceanum.client.actors

import akka.actor.Actor
import com.oceanum.common.{Ping, Pong}

/**
 * @author chenmingkun
 * @date 2020/6/28
 */
class HandlerActor[T](handler: T => Unit) extends Actor {
  override def receive: Receive = {
    case Ping => sender() ! Pong

    case t: T => handler(t)
  }
}
