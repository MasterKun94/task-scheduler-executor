package com.oceanum.exec

import com.oceanum.common.GraphContext

import scala.concurrent.{ExecutionContext, Future}

/**
 * @author chenmingkun
 * @date 2020/5/1
 */
trait TaskConfig {
  def close()

  def prepare(env: GraphContext)(implicit ec: ExecutionContext): Future[_<:TaskConfig]
}
