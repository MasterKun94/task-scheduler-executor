package com.oceanum.exec

import com.oceanum.common.ExprContext

import scala.concurrent.{ExecutionContext, Future}

/**
 * @author chenmingkun
 * @date 2020/5/1
 */
trait TaskConfig {
  def close()

  def prepare(env: ExprContext)(implicit ec: ExecutionContext): Future[_<:TaskConfig]
}
