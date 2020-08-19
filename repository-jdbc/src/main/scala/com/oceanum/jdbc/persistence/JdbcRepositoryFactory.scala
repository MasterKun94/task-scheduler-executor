package com.oceanum.jdbc.persistence

import com.oceanum.annotation.IRepositoryFactory
import com.oceanum.expr.JavaMap
import com.oceanum.jdbc.expr.JdbcExpressionFactory
import com.oceanum.persistence.{AbstractRepository, ExpressionFactory, Repository, RepositoryFactory}

import scala.concurrent.Future
import scala.reflect.runtime.universe._

@IRepositoryFactory(priority = 100)
class JdbcRepositoryFactory extends RepositoryFactory {
  override def create[T <: AnyRef](implicit tag: TypeTag[T]): Repository[T] = new AbstractRepository[T]() {
    private val fields: Map[String, Type] = typeOf(tag).members
      .collect {
        case m: MethodSymbol if m.isCaseAccessor => m.name.toString -> m.returnType
      }
      .toMap

    override def save(id: String, obj: T): Future[Unit] = ???

    override def save(obj: T): Future[String] = ???

    override def saveAll(objs: Seq[(String, T)]): Future[Unit] = ???

    override def findById(id: String): Future[Option[T]] = ???

    override def findByIdIn(ids: Seq[String]): Future[Seq[T]] = ???

    override def find(expr: String, env: JavaMap[String, AnyRef]): Future[Seq[T]] = ???
  }

  override def expressionFactory: ExpressionFactory = new JdbcExpressionFactory()
}
