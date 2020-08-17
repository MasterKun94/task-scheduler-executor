package com.oceanum.jdbc.expr

import ca.krasnay.sqlbuilder.SelectBuilder
import com.oceanum.api.Sort
import com.oceanum.persistence.{Expression, ExpressionFactory}

class JdbcExpressionFactory extends ExpressionFactory {
  override def select(query: Expression, sorts: Expression): Expression = {
    ???
  }

  override def select(query: Expression, sorts: Expression, size: Expression): Expression = ???

  override def select(query: Expression, sorts: Expression, size: Expression, page: Expression): Expression = ???

  private def build(select: SelectBuilder, expr: Expression): Unit = {

  }

  override def findAll(): Expression = ???

  override def term(field: String, value: AnyRef): Expression = ???

  override def terms(field: String, values: Array[AnyRef]): Expression = ???

  override def exists(field: String): Expression = ???

  override def sort(field: String, sortType: String): Expression = ???

  override def sorts(sorts: Array[Sort]): Expression = ???

  override def size(size: Int): Expression = ???

  override def page(page: Int): Expression = ???

  override def and(left: Expression, right: Expression): Expression = ???

  override def or(left: Expression, right: Expression): Expression = ???

  override def not(right: Expression): Expression = ???

  override def `match`(field: String, right: AnyRef): Expression = ???

  override def lt(left: Expression, right: AnyRef): Expression = ???

  override def lt(field: String, value: AnyRef): Expression = ???

  override def le(left: Expression, right: AnyRef): Expression = ???

  override def le(field: String, value: AnyRef): Expression = ???

  override def gt(left: Expression, right: AnyRef): Expression = ???

  override def gt(field: String, value: AnyRef): Expression = ???

  override def ge(left: Expression, right: AnyRef): Expression = ???

  override def ge(field: String, value: AnyRef): Expression = ???
}
