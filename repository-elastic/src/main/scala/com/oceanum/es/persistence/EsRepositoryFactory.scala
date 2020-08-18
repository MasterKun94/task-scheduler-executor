package com.oceanum.es.persistence

import com.oceanum.annotation.{IRepositoryFactory, ISerializationMessage}
import com.oceanum.es.expr.EsExpressionFactory
import com.oceanum.expr.{JavaHashMap, JavaMap}
import com.oceanum.persistence.{AbstractRepository, ExpressionFactory, Repository, RepositoryFactory}

import scala.concurrent.Future
import scala.reflect.runtime.universe.{TypeTag, typeOf}

/**
 * @author chenmingkun
 * @date 2020/8/1
 */
@IRepositoryFactory(priority = 1)
class EsRepositoryFactory extends RepositoryFactory {
  override def create[T <: AnyRef](implicit tag: TypeTag[T]): Repository[T] = new AbstractRepository[T]() {
    private implicit val mf: Manifest[T] = manifest
    private val index = {
      val clazz = tag.mirror.runtimeClass(typeOf(tag))
      if (clazz.isAnnotationPresent(classOf[ISerializationMessage]))
        clazz.getAnnotation(classOf[ISerializationMessage]).value().toLowerCase
      else
        clazz.getName.toLowerCase
    }

    override def save(id: String, obj: T): Future[Unit] = EsUtil.save[T](index, id, obj)

    override def save(obj: T): Future[String] = EsUtil.save[T](index, obj)

    override def saveAll(objs: Seq[(String, T)]): Future[Unit] = EsUtil.saveAll[T](index, objs)

    override def findById(id: String): Future[Option[T]] = EsUtil.findById[T](index, id)(mf)

    override def findByIdIn(ids: Seq[String]): Future[Seq[T]] = EsUtil.findByIdIn[T](index, ids)(mf)

    override def find(expr: String, env: JavaMap[String, AnyRef] = new JavaHashMap(0)): Future[Seq[T]] = EsUtil.find[T](index, expr, env)(mf)
  }

  override def expressionFactory: ExpressionFactory = new EsExpressionFactory()
}
