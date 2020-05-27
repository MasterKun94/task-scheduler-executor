package com.oceanum.common

import org.slf4j.{Logger, LoggerFactory}

/**
 * @author chenmingkun
 * @date 2020/4/30
 */
trait Log {
  val LOGGER: Logger = LoggerFactory.getLogger(this.getClass)
}
