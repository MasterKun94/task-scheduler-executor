package com.oceanum

import scala.concurrent.Future

/**
 * @author chenmingkun
 * @date 2020/6/28
 */
trait ShutdownHook {
  def kill(): Future[Boolean]
}
