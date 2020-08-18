package com.oceanum.persistence

import com.oceanum.annotation.IRepository
import com.oceanum.api.entities.WorkflowMetaInfo
import com.oceanum.common.{Environment, GraphMeta, TaskMeta}
import com.oceanum.expr.{JavaHashMap, JavaMap}

import scala.concurrent.{ExecutionContext, Future}

/**
 * @author chenmingkun
 * @date 2020/8/1
 */
@IRepository
class GraphMetaRepository extends AbstractRepository[GraphMeta] {
  private lazy val repo = Catalog.getRepository[WorkflowMetaInfo]
  private lazy val taskRepo = Catalog.getRepository[TaskMeta]
  private implicit lazy val ec: ExecutionContext = Environment.NONE_BLOCKING_EXECUTION_CONTEXT

  override def save(id: String, obj: GraphMeta): Future[Unit] = {
    val po = WorkflowMetaInfo.from(obj)
    val tm = obj.tasks.values.map(m => (MetaIdFactory.getTaskMetaId(obj, m), m))
    val f1 = repo.save(id, po)
    val f2 = taskRepo.saveAll(tm.toSeq)
    f1.flatMap(_ => f2)
  }

  override def save(obj: GraphMeta): Future[String] = {
    throw new UnsupportedOperationException
  }

    override def saveAll(objs: Seq[(String, GraphMeta)]): Future[Unit] = {
    val pos = objs.map(kv => (kv._1, WorkflowMetaInfo.from(kv._2)))
    val tms = objs
      .flatMap(t => t._2.tasks.map(m => (t._2, m._2)))
      .map(t => (MetaIdFactory.getTaskMetaId(t._1, t._2), t._2))
    val f1 = repo.saveAll(pos)
    val f2 = taskRepo.saveAll(tms)
    f1.flatMap(_ => f2)
  }

  override def findById(id: String): Future[Option[GraphMeta]] = {
    repo.findById(id)
      .flatMap {
        case Some(po) =>
          taskRepo.findByIdIn(po.tasks)
            .map(seq => Some(po.toMeta(seq)))
        case None => Future.successful(None)
      }
  }

  override def findByIdIn(ids: Seq[String]): Future[Seq[GraphMeta]] = convert(repo.findByIdIn(ids))

  override def find(expr: String, env: JavaMap[String, AnyRef] = new JavaHashMap(0)): Future[Seq[GraphMeta]] = convert(repo.find(expr, env))

  private def convert(future: Future[Seq[WorkflowMetaInfo]]): Future[Seq[GraphMeta]] = {
    future.flatMap { seq =>
      val futures = seq.map { po =>
        taskRepo.findByIdIn(po.tasks)
          .map(po.toMeta)
      }
      Future.sequence(futures)
    }
  }
}
