package com.oceanum.jdbc.expr

import com.oceanum.api.Sort
import com.oceanum.common.Environment
import com.oceanum.expr.Evaluator
import com.oceanum.persistence.{Expression, ExpressionFactory}
import tech.ibit.sqlbuilder.Sql

import scala.reflect.runtime.universe

class JdbcExpressionFactory extends ExpressionFactory {
  override def select(query: Expression, sorts: Expression): Expression = {
    SelectExpression(query.asInstanceOf[JdbcWhereExpression], sorts.asInstanceOf[SortsExpression])
  }

  override def select(query: Expression, sorts: Expression, size: Expression): Expression = {
    SelectExpression(query.asInstanceOf[JdbcWhereExpression], sorts.asInstanceOf[SortsExpression], size.asInstanceOf[LimitExpression])
  }

  override def select(query: Expression, sorts: Expression, size: Expression, page: Expression): Expression = {
    SelectExpression(query.asInstanceOf[JdbcWhereExpression], sorts.asInstanceOf[SortsExpression], size.asInstanceOf[LimitExpression], page.asInstanceOf[PageExpression])
  }

  override def findAll(): Expression = {
    EmptyExpression()
  }

  override def term(field: String, value: AnyRef): Expression = {
    EqualToExpression(field, value)
  }

  override def terms(field: String, values: Array[AnyRef]): Expression = {
    InExpression(field, values)
  }

  override def exists(field: String): Expression = {
    NotNullExpression(field)
  }

  override def sort(field: String, sortType: String): Expression = {
    SortsExpression(Array(Sort(field, sortType)))
  }

  override def sorts(sorts: Array[Sort]): Expression = {
    SortsExpression(sorts)
  }

  override def size(size: Int): Expression = {
    LimitExpression(size)
  }

  override def page(page: Int): Expression = {
    PageExpression(page)
  }

  override def and(left: Expression, right: Expression): Expression = {
    (left, right) match {
      case (l: JdbcWhereExpression, r: JdbcWhereExpression) =>
        AndExpression(l, r)
    }
  }

  override def or(left: Expression, right: Expression): Expression = {
    (left, right) match {
      case (l: JdbcWhereExpression, r: JdbcWhereExpression) =>
        OrExpression(l, r)
    }
  }

  override def not(right: Expression): Expression = {
    right match {
      case r: JdbcWhereExpression =>
        r.reverse()
    }
  }

  override def `match`(field: String, right: AnyRef): Expression = {
    LikeExpression(field, right.toString)
  }

  override def lt(left: Expression, right: AnyRef): Expression = {
    left match {
      case GtExpression(field, _) => and(left, lt(field, right))
      case GeExpression(field, _) => and(left, lt(field, right))
    }
  }

  override def lt(field: String, value: AnyRef): Expression = {
    LtExpression(field, value)
  }

  override def le(left: Expression, right: AnyRef): Expression = {
    left match {
      case GtExpression(field, _) => and(left, le(field, right))
      case GeExpression(field, _) => and(left, le(field, right))
    }
  }

  override def le(field: String, value: AnyRef): Expression = {
    LeExpression(field, value)
  }

  override def gt(left: Expression, right: AnyRef): Expression = {
    left match {
      case LtExpression(field, _) => and(left, gt(field, right))
      case LeExpression(field, _) => and(left, gt(field, right))
    }
  }

  override def gt(field: String, value: AnyRef): Expression = {
    GtExpression(field, value)
  }

  override def ge(left: Expression, right: AnyRef): Expression = {
    left match {
      case LtExpression(field, _) => and(left, ge(field, right))
      case LeExpression(field, _) => and(left, ge(field, right))
    }
  }

  override def ge(field: String, value: AnyRef): Expression = {
    GeExpression(field, value)
  }
}

object JdbcExpressionFactory {
  def main(args: Array[String]): Unit = {
//    Environment.loadEnv(Array("--conf=cluster/src/main/resources/application.properties"))
//    Environment.initSystem()
//    val env = Map(
//      "name" -> "name",
//      "sorts" -> Array(Sort("time", "DESC"))
//    )
//    import scala.collection.JavaConversions.mapAsJavaMap
//    val sql = Evaluator.rawExecute(
//      """
//        |repo.select(
//        | repo.field('name') == name,
//        | repo.sort(sorts),
//        | repo.size(1)
//        |)
//        |""".stripMargin, env)
//      .asInstanceOf[SelectExpression]
//      .createSql("table", "field1", "field2" )(new Sql())
//    println(sql.getSql)
//    println(sql.getParams)

    import scala.reflect.runtime.universe._
    val tag = typeTag[GtExpression]
    val calls: Map[String, MethodMirror] = tag.tpe.members
      .collect {
        case m: MethodSymbol if m.isCaseAccessor =>
          val key = hump2line(m.name.toString)
          val t = m.returnType
          println(key, t)
          key -> tag.mirror.reflect(GtExpression("field", "value")).reflectMethod(m)
      }
      .toMap
    println(calls)
    println(calls("value")())
    println(calls("field")())
  }

  private val pattern = "[A-Z]".r
  private def hump2line(hump: String): String = {
    pattern.replaceAllIn(hump, m => "_" + (hump.charAt(m.start) + 32).toChar)
  }
}
