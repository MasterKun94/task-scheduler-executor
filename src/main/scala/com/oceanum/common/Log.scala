package com.oceanum.common

import akka.event.{Logging, LoggingAdapter}

/**
 * @author chenmingkun
 * @date 2020/4/30
 */
trait Log {
  lazy val LOGGER: LoggingAdapter = Logging.getLogger(Environment.actorSystem, this)
}
