package com.oceanum.persistence

import com.oceanum.api.Sort

/**
 * @author chenmingkun
 * @date 2020/8/16
 */
trait ExpressionFactory {

  def select(query: Expression, sorts: Expression): Expression
  def select(query: Expression, sorts: Expression, size: Expression): Expression
  def select(query: Expression, sorts: Expression, size: Expression, page: Expression): Expression

  def findAll(): Expression

  def term(field: String, value: AnyRef): Expression

  def terms(field: String, values: Array[AnyRef]): Expression

  def exists(field: String): Expression

  def sort(field: String, sortType: String): Expression

  def sorts(sorts: Array[Sort]): Expression

  def size(size: Int): Expression

  def page(page: Int): Expression

  def and(left: Expression, right: Expression): Expression

  def or(left: Expression, right: Expression): Expression

  def not(right: Expression): Expression

  def eq(left: String, right: AnyRef): Expression = term(left, right)

  def neq(left: String, right: AnyRef): Expression = not(term(left, right))

  def `match`(field: String, right: AnyRef): Expression

  def lt(left: Expression, right: AnyRef): Expression

  def lt(field: String, value: AnyRef): Expression

  def le(left: Expression, right: AnyRef): Expression

  def le(field: String, value: AnyRef): Expression

  def gt(left: Expression, right: AnyRef): Expression

  def gt(field: String, value: AnyRef): Expression

  def ge(left: Expression, right: AnyRef): Expression

  def ge(field: String, value: AnyRef): Expression
}
