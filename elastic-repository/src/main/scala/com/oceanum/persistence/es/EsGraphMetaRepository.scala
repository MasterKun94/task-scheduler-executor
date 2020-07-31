package com.oceanum.persistence.es

import com.oceanum.annotation.IRepository
import com.oceanum.common.{GraphMeta, TaskMeta}
import com.oceanum.persistence.{AbstractRepository, Catalog}

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

@IRepository
class EsGraphMetaRepository extends AbstractRepository[GraphMeta] {
  private implicit val ec: ExecutionContext = ExecutionContext.global
  private val taskRepo = Catalog.getRepository[TaskMeta]

  override def save(id: String, obj: GraphMeta): Future[Unit] = {
    val esGraphMeta: EsGraphMeta = EsMetaUtil.convertGraphMeta(obj)
    val future = EsUtil.save("graph-meta", id, esGraphMeta)
    val futures = obj.tasks.values.map(m => taskRepo.save(EsMetaUtil.getTaskMetaId(m, obj), m)).toArray
    (futures :+ future).reduce((_, _) => null)
  }

  override def findById(id: String): Future[Option[GraphMeta]] = {
    val future: Future[Option[EsGraphMeta]] = EsUtil.findById[EsGraphMeta]("graph-meta", id)
    future.map { _
      .map { esGraphMeta =>
        val f: Future[Map[Int, TaskMeta]] = Future
          .sequence(esGraphMeta.tasks.map(taskRepo.findById))
          .map(_
            .filter(_.nonEmpty)
            .map(option => (option.get.id -> option.get))
            .toMap
          )
        f.map { map =>
          EsMetaUtil.convertGraphMeta()

        }
      }
    }
  }

  override def find(expr: String): Future[Array[GraphMeta]] = {
    EsUtil.find("graph-meta", expr)
  }
}