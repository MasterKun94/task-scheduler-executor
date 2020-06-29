package com.oceanum.common

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}

/**
 * @author chenmingkun
 * @date 2020/4/30
 */
class Log(actorSystem: => ActorSystem = Environment.CLUSTER_NODE_SYSTEM) {
  lazy val log: LoggingAdapter = Logging.getLogger(actorSystem, this)
  implicit lazy val system: ActorSystem = actorSystem
}
