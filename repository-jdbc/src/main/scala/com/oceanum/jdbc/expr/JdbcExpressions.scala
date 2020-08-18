package com.oceanum.jdbc.expr

import java.util
import java.util.Collections

import com.oceanum.api.Sort
import com.oceanum.persistence.Expression
import tech.ibit.sqlbuilder.{Criteria, Table, CriteriaItemMaker => maker}

trait JdbcWhereExpression extends Expression {
  def reverse(): JdbcWhereExpression
  def criteria(implicit table: Table): Criteria
}
case class EmptyExpression() extends JdbcWhereExpression {
  override def reverse(): JdbcWhereExpression = throw new UnsupportedOperationException()

  override def criteria(implicit table: Table): Criteria = Criteria.and(Collections.emptyList[Criteria]())
}
case class EqualToExpression(field: String, value: AnyRef) extends JdbcWhereExpression {
  override def reverse(): JdbcWhereExpression = NotEqualToExpression(field, value)

  override def criteria(implicit table: Table): Criteria = maker.equalsTo(field, value)
}
case class NotEqualToExpression(field: String, value: AnyRef) extends JdbcWhereExpression {
  override def reverse(): JdbcWhereExpression = EqualToExpression(field, value)

  override def criteria(implicit table: Table): Criteria = maker.notEqualsTo(field, value)
}
case class InExpression(field: String, value: Array[AnyRef]) extends JdbcWhereExpression {
  override def reverse(): JdbcWhereExpression = NotInExpression(field, value)

  override def criteria(implicit table: Table): Criteria = maker.in(field, util.Arrays.asList(value))
}
case class NotInExpression(field: String, value: Array[AnyRef]) extends JdbcWhereExpression {
  override def reverse(): JdbcWhereExpression = InExpression(field, value)

  override def criteria(implicit table: Table): Criteria = maker.notIn(field, util.Arrays.asList(value))
}
case class IsNullExpression(field: String) extends JdbcWhereExpression {
  override def reverse(): JdbcWhereExpression = NotNullExpression(field)

  override def criteria(implicit table: Table): Criteria = maker.isNull(field)
}
case class NotNullExpression(field: String) extends JdbcWhereExpression {
  override def reverse(): JdbcWhereExpression = IsNullExpression(field)

  override def criteria(implicit table: Table): Criteria = maker.isNotNull(field)
}
case class AndExpression(left: JdbcWhereExpression, right: JdbcWhereExpression) extends JdbcWhereExpression {
  override def reverse(): JdbcWhereExpression = OrExpression(left.reverse(), right.reverse())

  override def criteria(implicit table: Table): Criteria = Criteria.and(util.Arrays.asList(left.criteria, right.criteria))
}
case class OrExpression(left: JdbcWhereExpression, right: JdbcWhereExpression) extends JdbcWhereExpression {
  override def reverse(): JdbcWhereExpression = AndExpression(left.reverse(), right.reverse())

  override def criteria(implicit table: Table): Criteria = Criteria.or(util.Arrays.asList(left.criteria, right.criteria))
}
case class LikeExpression(field: String, value: String) extends JdbcWhereExpression {
  override def reverse(): JdbcWhereExpression = NotLikeExpression(field, value)

  override def criteria(implicit table: Table): Criteria = maker.like(field, value)
}
case class NotLikeExpression(field: String, value: String) extends JdbcWhereExpression {
  override def reverse(): JdbcWhereExpression = LikeExpression(field, value)

  override def criteria(implicit table: Table): Criteria = maker.notLike(field, value)
}
case class LtExpression(field: String, value: AnyRef) extends JdbcWhereExpression {
  override def reverse(): JdbcWhereExpression = GeExpression(field, value)

  override def criteria(implicit table: Table): Criteria = maker.lessThan(field, value)
}
case class LeExpression(field: String, value: AnyRef) extends JdbcWhereExpression {
  override def reverse(): JdbcWhereExpression = GtExpression(field, value)

  override def criteria(implicit table: Table): Criteria = maker.lessThanOrEqualTo(field, value)
}
case class GtExpression(field: String, value: AnyRef) extends JdbcWhereExpression {
  override def reverse(): JdbcWhereExpression = LeExpression(field, value)

  override def criteria(implicit table: Table): Criteria = maker.greaterThan(field, value)
}
case class GeExpression(field: String, value: AnyRef) extends JdbcWhereExpression {
  override def reverse(): JdbcWhereExpression = LtExpression(field, value)

  override def criteria(implicit table: Table): Criteria = maker.greaterThanOrEqualsTo(field, value)
}
case class LimitExpression(limit: Int = 0) extends Expression
case class PageExpression(limit: Int = 0) extends Expression
case class SortsExpression(sorts: Array[Sort]) extends Expression
case class SelectExpression(where: JdbcWhereExpression = EmptyExpression(), sorts: SortsExpression = SortsExpression(Array.empty), limit: LimitExpression = LimitExpression(10), page: PageExpression = PageExpression(0)) extends Expression