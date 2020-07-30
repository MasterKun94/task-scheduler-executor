package com.oceanum.persistence.es

import java.util.Date

import com.oceanum.common.{FallbackStrategy, GraphMeta, GraphStatus, ReRunStrategy}
import com.oceanum.persistence.{AbstractRepository, Catalog, Repository}

import scala.concurrent.Future
import scala.util.{Failure, Success}

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