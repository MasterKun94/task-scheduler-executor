package com.oceanum.persistence.es

import com.oceanum.annotation.{InjectType, Injection}
import com.oceanum.common.GraphMeta
import com.oceanum.persistence.AbstractRepository

import scala.concurrent.Future

@Injection(InjectType.REPOSITORY)
class EsGraphMetaRepository extends AbstractRepository[GraphMeta] {

  override def save(obj: GraphMeta): Future[Unit] = {
    EsUtil.save("graph-meta", s"${obj.name}-${obj.id}-${obj.reRunId}", obj)
  }

  override def findById(id: String): Future[Option[GraphMeta]] = {
    EsUtil.findById("graph-meta", id)
  }

  override def find(expr: String): Future[Array[GraphMeta]] = {
    EsUtil.find("graph-meta", expr)
  }
}