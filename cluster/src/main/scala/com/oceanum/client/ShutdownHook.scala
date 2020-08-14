package com.oceanum.client

import scala.concurrent.Future

/**
 * @author chenmingkun
 * @date 2020/6/28
 */
@deprecated
trait ShutdownHook {
  def kill(): Future[Boolean]
}
