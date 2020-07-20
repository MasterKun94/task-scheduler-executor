package com.oceanum.cluster.tasks

import com.oceanum.client.TaskMeta
import com.oceanum.cluster.exec.TaskConfig

import scala.concurrent.{ExecutionContext, Future}

/**
 * @author chenmingkun
 * @date 2020/7/20
 */
class SparkTaskConfig extends TaskConfig {
  override def close(): Unit = {

  }

  override def prepare(taskMeta: TaskMeta, env: Map[String, Any])(implicit ec: ExecutionContext): Future[_ <: TaskConfig] = {
    Future.successful(this)
  }
}
