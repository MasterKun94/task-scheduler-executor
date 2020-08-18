package com.oceanum.jdbc.expr

import com.oceanum.api.Sort
import com.oceanum.persistence.{Expression, ExpressionFactory}

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
  import scala.reflect.runtime.universe._
  def mf[T](implicit mf: Manifest[T]): Manifest[T] = mf

  def tag[A](implicit typeTag: TypeTag[A]): Type = typeOf(typeTag)

  def classAccessors[T: TypeTag]: List[MethodSymbol] = typeOf[T].members.collect {
    case m: MethodSymbol if m.isCaseAccessor => m
  }.toList

  def main(args: Array[String]): Unit = {
    classAccessors[GtExpression].map(_.name.toString).foreach(println)
  }
}
