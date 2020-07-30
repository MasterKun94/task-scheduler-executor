package com.oceanum.persistence.es

import com.oceanum.common.TaskMeta
import com.oceanum.persistence.AbstractRepository

import scala.concurrent.Future

class EsTaskMetaRepository extends AbstractRepository[TaskMeta] {

  override def save(obj: TaskMeta): Future[Unit] = {
    EsUtil.save("task-meta", s"${obj.name}-${obj.id}-${obj.reRunId}", obj)
  }

  override def findById(id: String): Future[Option[TaskMeta]] = {
    EsUtil.findById("task-meta", id)
  }

  override def find(expr: String): Future[Array[TaskMeta]] = {
    EsUtil.find("task-meta", expr)
  }
}
