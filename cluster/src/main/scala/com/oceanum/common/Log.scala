package com.oceanum.common

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}

/**
 * @author chenmingkun
 * @date 2020/4/30
 */
class Log(actorSystem: => ActorSystem = ActorSystems.CLUSTER_SYSTEM) {
  protected lazy val log: LoggingAdapter = Logging.getLogger(actorSystem, this)
  protected implicit lazy val system: ActorSystem = actorSystem
}
