package com.oceanum.es.persistence

import com.oceanum.annotation.IRepository
import com.oceanum.api.entities.TaskMetaInfo
import com.oceanum.expr.{JavaHashMap, JavaMap}
import com.oceanum.persistence.AbstractRepository

import scala.concurrent.Future

@IRepository
class EsTaskMetaPoRepository extends AbstractRepository[TaskMetaInfo] {
  private val index = "task-meta"

  override def save(id: String, obj: TaskMetaInfo): Future[Unit] = EsUtil.save(index, id, obj)

  override def save(obj: TaskMetaInfo): Future[String] = EsUtil.save(index, obj)

  override def saveAll(objs: Seq[(String, TaskMetaInfo)]): Future[Unit] = EsUtil.saveAll(index, objs)

  override def findById(id: String): Future[Option[TaskMetaInfo]] = EsUtil.findById(index, id)

  override def findByIdIn(ids: Seq[String]): Future[Seq[TaskMetaInfo]] = EsUtil.findByIdIn(index, ids)

  override def find(expr: String, env: JavaMap[String, AnyRef] = new JavaHashMap(0)): Future[Seq[TaskMetaInfo]] = EsUtil.find(index, expr, env)
}
