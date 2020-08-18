package com.oceanum.es.expr

import com.oceanum.api.Sort
import com.oceanum.persistence.{Expression, ExpressionFactory}
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.sort.{SortBuilders, SortOrder}

/**
 * @author chenmingkun
 * @date 2020/8/16
 */
class EsExpressionFactory extends ExpressionFactory {

  override def select(query: Expression, sorts: Expression): Expression = {
    val searchSourceBuilder = SearchSourceBuilder.searchSource()
    build(searchSourceBuilder, query)
    build(searchSourceBuilder, sorts)
    SearchSourceExpression(searchSourceBuilder)
  }

  override def select(query: Expression, sorts: Expression, size: Expression): Expression = {
    val searchSourceBuilder = SearchSourceBuilder.searchSource()
    build(searchSourceBuilder, query)
    build(searchSourceBuilder, sorts)
    build(searchSourceBuilder, size)
    SearchSourceExpression(searchSourceBuilder)
  }

  override def select(query: Expression, sorts: Expression, size: Expression, page: Expression): Expression = {
    val searchSourceBuilder = SearchSourceBuilder.searchSource()
    build(searchSourceBuilder, query)
    build(searchSourceBuilder, sorts)
    build(searchSourceBuilder, size)
    build(searchSourceBuilder, page)
    SearchSourceExpression(searchSourceBuilder)
  }
  private def build(searchSourceBuilder: SearchSourceBuilder, expr: Expression): Unit = {
    expr match {
      case e: QueryExpression => searchSourceBuilder.query(e.queryBuilder)
      case e: RangeExpression => searchSourceBuilder.query(e.rangeQueryBuilder)
      case e: SortsExpression => e.sortBuilders.foreach(builder => searchSourceBuilder.sort(builder))
      case e: SizeExpression => searchSourceBuilder.size(e.size)
      case e: PageExpression => searchSourceBuilder.from(e.page * searchSourceBuilder.size())
    }
  }
  override def findAll(): Expression = {
    QueryExpression(QueryBuilders.matchAllQuery())
  }

  override def term(field: String, value: AnyRef): Expression = {
    QueryExpression(QueryBuilders.termQuery(field, value))
  }

  override def terms(field: String, values: Array[AnyRef]): Expression = {
    QueryExpression(QueryBuilders.termsQuery(field, values: _*))
  }

  override def exists(field: String): Expression = {
    QueryExpression(QueryBuilders.existsQuery(field))
  }

  override def sort(field: String, sortType: String): Expression = {
    SortsExpression(Array(SortBuilders.fieldSort(field).order(SortOrder.valueOf(sortType.toUpperCase()))))
  }

  override def sorts(sorts: Array[Sort]): Expression = {
    SortsExpression(sorts.map(sort => SortBuilders.fieldSort(sort.field).order(SortOrder.valueOf(sort.order.toUpperCase()))))
  }

  override def size(size: Int): Expression = {
    SizeExpression(size)
  }

  override def page(page: Int): Expression = {
    PageExpression(page)
  }

  override def and(left: Expression, right: Expression): Expression = (left, right) match {
    case (QueryExpression(q1), QueryExpression(q2)) =>
      QueryExpression(QueryBuilders.boolQuery().must(q1).must(q2))
  }

  override def or(left: Expression, right: Expression): Expression = (left, right) match {
    case (QueryExpression(q1), QueryExpression(q2)) =>
      QueryExpression(QueryBuilders.boolQuery().should(q1).should(q2))
  }

  override def not(right: Expression): Expression = right match {
    case QueryExpression(q1) =>
      QueryExpression(QueryBuilders.boolQuery().mustNot(q1))
  }

  override def `match`(field: String, right: AnyRef): Expression = {
    QueryExpression(QueryBuilders.matchQuery(field, right))
  }

  override def lt(left: Expression, right: AnyRef): Expression = left match {
    case RangeExpression(q) => QueryExpression(q.lt(right))
  }

  override def lt(field: String, value: AnyRef): Expression = {
    QueryExpression(QueryBuilders.rangeQuery(field).lt(value))
  }

  override def le(left: Expression, right: AnyRef): Expression = left match {
    case RangeExpression(q) => QueryExpression(q.lte(right))
  }

  override def le(field: String, value: AnyRef): Expression = {
    QueryExpression(QueryBuilders.rangeQuery(field).lte(value))
  }

  override def gt(left: Expression, right: AnyRef): Expression = left match {
    case RangeExpression(q) => QueryExpression(q.gt(right))
  }

  override def gt(field: String, value: AnyRef): Expression = {
    QueryExpression(QueryBuilders.rangeQuery(field).gt(value))
  }

  override def ge(left: Expression, right: AnyRef): Expression = left match {
    case RangeExpression(q) => QueryExpression(q.gte(right))
  }

  override def ge(field: String, value: AnyRef): Expression = {
    QueryExpression(QueryBuilders.rangeQuery(field).gte(value))
  }
}
