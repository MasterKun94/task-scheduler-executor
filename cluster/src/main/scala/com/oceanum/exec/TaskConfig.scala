package com.oceanum.exec

import com.oceanum.client.TaskMeta

import scala.concurrent.{ExecutionContext, Future}

/**
 * @author chenmingkun
 * @date 2020/5/1
 */
trait TaskConfig {
  def close()

  def prepare(env: Map[String, Any])(implicit ec: ExecutionContext): Future[_<:TaskConfig]
}
