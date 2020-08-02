package com.oceanum.expr

import com.googlecode.aviator.runtime.`type`.{AviatorObject, AviatorRuntimeJavaType}
import com.googlecode.aviator.runtime.function.{AbstractFunction, FunctionUtils}
import com.oceanum.annotation.IFunction
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.search.aggregations.AggregationBuilder
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.sort.SortBuilder

@IFunction
class EsSearchFunction extends AbstractFunction {
  override def getName: String = "es.search"

  override def call(env: JavaMap[String, AnyRef], query: AviatorObject): AviatorObject = {
    val sourceBuilder = new SearchSourceBuilder()
    sourceBuilder.query(FunctionUtils.getJavaObject(query, env).asInstanceOf[QueryBuilder])
    AviatorRuntimeJavaType.valueOf(sourceBuilder)
  }
  
  override def call(env: JavaMap[String, AnyRef], query: AviatorObject, sortOrAggr: AviatorObject): AviatorObject = {
    val sourceBuilder = new SearchSourceBuilder()
    sourceBuilder.query(FunctionUtils.getJavaObject(query, env).asInstanceOf[QueryBuilder])
    sortOrAggr.getValue(env) match {
      case sort: SortBuilder[_] => sourceBuilder.sort(sort)
      case aggr: AggregationBuilder => sourceBuilder.aggregation(aggr)
    }
    AviatorRuntimeJavaType.valueOf(sourceBuilder)
  }
}