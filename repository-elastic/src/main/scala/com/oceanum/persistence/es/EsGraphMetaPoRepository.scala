package com.oceanum.persistence.es

import com.oceanum.annotation.IRepository
import com.oceanum.api.entities.WorkflowMetaInfo
import com.oceanum.expr.{JavaHashMap, JavaMap}
import com.oceanum.persistence.AbstractRepository

import scala.concurrent.Future

@IRepository
class EsGraphMetaPoRepository extends AbstractRepository[WorkflowMetaInfo] {
  private val index = "graph-meta"

  override def save(id: String, obj: WorkflowMetaInfo): Future[Unit] = EsUtil.save(index, id, obj)

  override def save(obj: WorkflowMetaInfo): Future[String] = EsUtil.save(index, obj)

  override def saveAll(objs: Seq[(String, WorkflowMetaInfo)]): Future[Unit] = EsUtil.saveAll(index, objs)

  override def findById(id: String): Future[Option[WorkflowMetaInfo]] = EsUtil.findById(index, id)

  override def findByIdIn(ids: Seq[String]): Future[Seq[WorkflowMetaInfo]] = EsUtil.findByIdIn(index, ids)

  override def find(expr: String, env: JavaMap[String, AnyRef]): Future[Seq[WorkflowMetaInfo]] = EsUtil.find(index, expr, env)
}