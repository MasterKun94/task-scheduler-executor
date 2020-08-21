package com.oceanum.jdbc.persistence

import com.oceanum.annotation.{IRepositoryFactory, ISerializationMessage}
import com.oceanum.api.Sort
import com.oceanum.common.Environment
import com.oceanum.expr.{Evaluator, JavaMap}
import com.oceanum.jdbc.expr.{JdbcExpressionFactory, JdbcWhereExpression, LimitExpression, PageExpression, SelectExpression, SortsExpression}
import com.oceanum.persistence.{AbstractRepository, ExpressionFactory, Repository, RepositoryFactory}
import tech.ibit.sqlbuilder.Sql

import scala.concurrent.Future
import scala.reflect.runtime.universe._

@IRepositoryFactory(priority = 100)
class JdbcRepositoryFactory extends RepositoryFactory {

  override def create[T <: AnyRef](implicit tag: TypeTag[T]): Repository[T] = new AbstractRepository[T]() {
    private val clazz = tag.mirror.runtimeClass(typeOf(tag))

    private val idField: String = "_id"
    private val fields: Map[String, T => Any] = typeOf(tag).members
      .collect {
        case m: MethodSymbol if m.isCaseAccessor =>
          val key = hump2line(m.name.toString)
          val field: T => Any = tag.mirror.reflect(_).reflectMethod(m)
          key -> field
      }
      .toMap
    private val objFieldNames = fields.keys
    private val fieldNames = Array(idField) ++ objFieldNames
    private val fieldString = (fieldNames).mkString(",")
    private val fieldNumber = fieldNames.length

    private val tbName = hump2line {
      if (clazz.isAnnotationPresent(classOf[ISerializationMessage]))
        clazz.getAnnotation(classOf[ISerializationMessage]).value().toLowerCase
      else
        clazz.getSimpleName.toLowerCase.replace(".", "_")
    }

    private val insertSql = s"insert into $tbName ($fieldString) values (${Seq.fill(fieldNumber)("?").mkString(",")})"
    override def save(id: String, obj: T): Future[Unit] = ???

    override def save(obj: T): Future[String] = ???

    override def saveAll(objs: Seq[(String, T)]): Future[Unit] = ???

    private val selectSql = s"select $fieldString from $tbName where $idField=?"
    override def findById(id: String): Future[Option[T]] = ???

    private val selectInSql = s"select $fieldString from $tbName where $idField in "
    private def selectInSql(size: Int): String = selectInSql + ${Seq.fill(size)("?").mkString("(", ",", ")")}
    override def findByIdIn(ids: Seq[String]): Future[Seq[T]] = ???

    override def find(expr: String, env: JavaMap[String, AnyRef]): Future[Seq[T]] = {
      val expr = Evaluator.rawExecute(expr, env) match {
        case e: SelectExpression =>
          e
        case e: JdbcWhereExpression =>
          SelectExpression(where = e)
        case e: SortsExpression =>
          SelectExpression(sorts = e)
        case e: LimitExpression =>
          SelectExpression(limit = e)
        case e: PageExpression =>
          SelectExpression(page = e)
      }
      val sql = expr.createSql(tbName, fieldNames: _*)(new Sql())
      ???
    }
  }

  override def expressionFactory: ExpressionFactory = new JdbcExpressionFactory()

  private val pattern = "[A-Z]".r
  private def hump2line(hump: String): String = {
    pattern.replaceAllIn(hump, m => "_" + (hump.charAt(m.start) + 32).toChar)
  }
}
