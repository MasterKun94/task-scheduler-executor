package com.oceanum.es.expr

import com.oceanum.persistence.Expression
import org.elasticsearch.index.query.{QueryBuilder, RangeQueryBuilder}
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.sort.SortBuilder

/**
 * @author chenmingkun
 * @date 2020/8/16
 */
case class SearchSourceExpression(searchSourceBuilder: SearchSourceBuilder) extends Expression
case class QueryExpression(queryBuilder: QueryBuilder) extends Expression
case class RangeExpression(rangeQueryBuilder: RangeQueryBuilder) extends Expression
case class SortsExpression(sortBuilders: Array[SortBuilder[_]]) extends Expression
case class SizeExpression(size: Int) extends Expression
case class PageExpression(page: Int) extends Expression
