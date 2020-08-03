package com.oceanum.persistence.es

import com.oceanum.annotation.{IRepositoryFactory, ISerializationMessage}
import com.oceanum.expr.{JavaHashMap, JavaMap}
import com.oceanum.persistence.{AbstractRepository, Repository, RepositoryFactory}

import scala.concurrent.Future

/**
 * @author chenmingkun
 * @date 2020/8/1
 */
@IRepositoryFactory(priority = 1)
class EsRepositoryFactory extends RepositoryFactory {
  override def create[T <: AnyRef](implicit mf: Manifest[T]): Repository[T] = new AbstractRepository[T]() {
    private val index = {
      val clazz = mf.runtimeClass
      if (clazz.isAnnotationPresent(classOf[ISerializationMessage]))
        clazz.getAnnotation(classOf[ISerializationMessage]).value().toLowerCase
      else
        mf.runtimeClass.getName.toLowerCase
    }

    override def save(id: String, obj: T): Future[Unit] = EsUtil.save(index, id, obj)

    override def saveAll(objs: Seq[(String, T)]): Future[Unit] = EsUtil.saveAll(index, objs)

    override def findById(id: String): Future[Option[T]] = EsUtil.findById(index, id)

    override def findByIdIn(ids: Seq[String]): Future[Seq[T]] = EsUtil.findByIdIn(index, ids)

    override def find(expr: String, env: JavaMap[String, AnyRef] = new JavaHashMap(0)): Future[Seq[T]] = EsUtil.find(index, expr, env)
  }
}
