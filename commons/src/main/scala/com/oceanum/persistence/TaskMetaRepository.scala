package com.oceanum.persistence

import com.oceanum.annotation.IRepository
import com.oceanum.api.entities.TaskMetaInfo
import com.oceanum.common.{Environment, TaskMeta}
import com.oceanum.jdbc.expr.{JavaHashMap, JavaMap}

import scala.concurrent.{ExecutionContext, Future}

/**
 * @author chenmingkun
 * @date 2020/8/1
 */
@IRepository
class TaskMetaRepository extends AbstractRepository[TaskMeta] {
  private lazy val repo = Catalog.getRepository[TaskMetaInfo]
  private implicit lazy val ec: ExecutionContext = Environment.NONE_BLOCKING_EXECUTION_CONTEXT

  override def save(id: String, obj: TaskMeta): Future[Unit] = repo.save(id, TaskMetaInfo.from(obj))

  override def save(obj: TaskMeta): Future[String] = repo.save(TaskMetaInfo.from(obj))

  override def saveAll(objs: Seq[(String, TaskMeta)]): Future[Unit] = repo.saveAll(objs.map(kv => (kv._1, TaskMetaInfo.from(kv._2))))

  override def findById(id: String): Future[Option[TaskMeta]] = repo.findById(id).map(_.map(_.toMeta))

  override def findByIdIn(ids: Seq[String]): Future[Seq[TaskMeta]] = repo.findByIdIn(ids).map(_.map(_.toMeta))

  override def find(expr: String, env: JavaMap[String, AnyRef] = new JavaHashMap(0)): Future[Seq[TaskMeta]] = repo.find(expr, env).map(_.map(_.toMeta))
}
