package com.oceanum.jdbc.expr

import com.googlecode.aviator.runtime.`type`.{AviatorObject, AviatorRuntimeJavaType}
import com.googlecode.aviator.runtime.function.{AbstractFunction, FunctionUtils}
import com.oceanum.annotation.IFunction
import org.elasticsearch.index.query.{BoolQueryBuilder, QueryBuilder, QueryBuilders, RangeQueryBuilder}
import org.elasticsearch.search.aggregations.AggregationBuilder
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.sort.SortBuilder

/**
 * @author chenmingkun
 * @date 2020/8/2
 */
@IFunction
class EsMatchAllFunction extends AbstractFunction {
  override def getName: String = "es.matchAll"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = {
    AviatorRuntimeJavaType.valueOf(QueryBuilders.matchAllQuery())
  }
}

@IFunction
class EsMatchFunction extends AbstractFunction {
  override def getName: String = "es.match"

  override def call(env: JavaMap[String, AnyRef], field: AviatorObject, value: AviatorObject): AviatorObject = {
    val fieldString = FunctionUtils.getStringValue(field, env)
    val valueObject = value.getValue(env)
    AviatorRuntimeJavaType.valueOf(QueryBuilders.matchQuery(fieldString, valueObject))
  }
}

@IFunction
class EsRangeFunction extends AbstractFunction {
  override def getName: String = "es.range"

  override def call(env: JavaMap[String, AnyRef], field: AviatorObject, range: AviatorObject): AviatorObject = {
    val fieldString = FunctionUtils.getStringValue(field, env)
    val rangeQueryBuilder = QueryBuilders.rangeQuery(fieldString)
    range.getValue(env).asInstanceOf[EsElement[RangeQueryBuilder]].create(rangeQueryBuilder)
    AviatorRuntimeJavaType.valueOf(rangeQueryBuilder)
  }

  override def call(env: JavaMap[String, AnyRef], field: AviatorObject, range1: AviatorObject, range2: AviatorObject): AviatorObject = {
    val fieldString = FunctionUtils.getStringValue(field, env)
    val rangeQueryBuilder = QueryBuilders.rangeQuery(fieldString)
    range1.getValue(env).asInstanceOf[EsElement[RangeQueryBuilder]].create(rangeQueryBuilder)
    range2.getValue(env).asInstanceOf[EsElement[RangeQueryBuilder]].create(rangeQueryBuilder)
    AviatorRuntimeJavaType.valueOf(rangeQueryBuilder)
  }
}

@IFunction
class EsTermFunction extends AbstractFunction {
  override def getName: String = "es.term"

  override def call(env: JavaMap[String, AnyRef], field: AviatorObject, value: AviatorObject): AviatorObject = {
    val fieldString = FunctionUtils.getStringValue(field, env)
    val valueObject = value.getValue(env)
    AviatorRuntimeJavaType.valueOf(QueryBuilders.termQuery(fieldString, valueObject))
  }
}

@IFunction
class EsTermsFunction extends AbstractFunction {
  override def getName: String = "es.terms"

  override def call(env: JavaMap[String, AnyRef], field: AviatorObject, value: AviatorObject): AviatorObject = {
    val fieldString = FunctionUtils.getStringValue(field, env)
    val valueObject = value.getValue(env)
    AviatorRuntimeJavaType.valueOf(QueryBuilders.termsQuery(fieldString, valueObject))
  }
  override def call(env: JavaMap[String, AnyRef], field: AviatorObject, value: AviatorObject, value2: AviatorObject): AviatorObject = {
    val fieldString = FunctionUtils.getStringValue(field, env)
    val valueObject = value.getValue(env)
    val valueObject2 = value2.getValue(env)
    AviatorRuntimeJavaType.valueOf(QueryBuilders.termsQuery(fieldString, valueObject, valueObject2))
  }
  override def call(env: JavaMap[String, AnyRef], field: AviatorObject, value: AviatorObject, value2: AviatorObject, value3: AviatorObject): AviatorObject = {
    val fieldString = FunctionUtils.getStringValue(field, env)
    val valueObject = value.getValue(env)
    val valueObject2 = value2.getValue(env)
    val valueObject3 = value3.getValue(env)
    AviatorRuntimeJavaType.valueOf(QueryBuilders.termsQuery(fieldString, valueObject, valueObject2, valueObject3))
  }
  override def call(env: JavaMap[String, AnyRef], field: AviatorObject, value: AviatorObject, value2: AviatorObject, value3: AviatorObject, value4: AviatorObject): AviatorObject = {
    val fieldString = FunctionUtils.getStringValue(field, env)
    val valueObject = value.getValue(env)
    val valueObject2 = value2.getValue(env)
    val valueObject3 = value3.getValue(env)
    val valueObject4 = value4.getValue(env)
    AviatorRuntimeJavaType.valueOf(QueryBuilders.termsQuery(fieldString, valueObject, valueObject2, valueObject3, valueObject4))
  }
  override def call(env: JavaMap[String, AnyRef], field: AviatorObject, value: AviatorObject, value2: AviatorObject, value3: AviatorObject, value4: AviatorObject, value5: AviatorObject): AviatorObject = {
    val fieldString = FunctionUtils.getStringValue(field, env)
    val valueObject = value.getValue(env)
    val valueObject2 = value2.getValue(env)
    val valueObject3 = value3.getValue(env)
    val valueObject4 = value4.getValue(env)
    val valueObject5 = value5.getValue(env)
    AviatorRuntimeJavaType.valueOf(QueryBuilders.termsQuery(fieldString, valueObject, valueObject2, valueObject3, valueObject4, valueObject5))
  }
  override def call(env: JavaMap[String, AnyRef], field: AviatorObject, value: AviatorObject, value2: AviatorObject, value3: AviatorObject, value4: AviatorObject, value5: AviatorObject, value6: AviatorObject): AviatorObject = {
    val fieldString = FunctionUtils.getStringValue(field, env)
    val valueObject = value.getValue(env)
    val valueObject2 = value2.getValue(env)
    val valueObject3 = value3.getValue(env)
    val valueObject4 = value4.getValue(env)
    val valueObject5 = value5.getValue(env)
    val valueObject6 = value6.getValue(env)
    AviatorRuntimeJavaType.valueOf(QueryBuilders.termsQuery(fieldString, valueObject, valueObject2, valueObject3, valueObject4, valueObject5, valueObject6))
  }
  override def call(env: JavaMap[String, AnyRef], field: AviatorObject, value: AviatorObject, value2: AviatorObject, value3: AviatorObject, value4: AviatorObject, value5: AviatorObject, value6: AviatorObject, value7: AviatorObject): AviatorObject = {
    val fieldString = FunctionUtils.getStringValue(field, env)
    val valueObject = value.getValue(env)
    val valueObject2 = value2.getValue(env)
    val valueObject3 = value3.getValue(env)
    val valueObject4 = value4.getValue(env)
    val valueObject5 = value5.getValue(env)
    val valueObject6 = value6.getValue(env)
    val valueObject7 = value7.getValue(env)
    AviatorRuntimeJavaType.valueOf(QueryBuilders.termsQuery(fieldString, valueObject, valueObject2, valueObject3, valueObject4, valueObject5, valueObject6, valueObject7))
  }
  override def call(env: JavaMap[String, AnyRef], field: AviatorObject, value: AviatorObject, value2: AviatorObject, value3: AviatorObject, value4: AviatorObject, value5: AviatorObject, value6: AviatorObject, value7: AviatorObject, value8: AviatorObject): AviatorObject = {
    val fieldString = FunctionUtils.getStringValue(field, env)
    val valueObject = value.getValue(env)
    val valueObject2 = value2.getValue(env)
    val valueObject3 = value3.getValue(env)
    val valueObject4 = value4.getValue(env)
    val valueObject5 = value5.getValue(env)
    val valueObject6 = value6.getValue(env)
    val valueObject7 = value7.getValue(env)
    val valueObject8 = value8.getValue(env)
    AviatorRuntimeJavaType.valueOf(QueryBuilders.termsQuery(fieldString, valueObject, valueObject2, valueObject3, valueObject4, valueObject5, valueObject6, valueObject7, valueObject8))
  }
  override def call(env: JavaMap[String, AnyRef], field: AviatorObject, value: AviatorObject, value2: AviatorObject, value3: AviatorObject, value4: AviatorObject, value5: AviatorObject, value6: AviatorObject, value7: AviatorObject, value8: AviatorObject, value9: AviatorObject): AviatorObject = {
    val fieldString = FunctionUtils.getStringValue(field, env)
    val valueObject = value.getValue(env)
    val valueObject2 = value2.getValue(env)
    val valueObject3 = value3.getValue(env)
    val valueObject4 = value4.getValue(env)
    val valueObject5 = value5.getValue(env)
    val valueObject6 = value6.getValue(env)
    val valueObject7 = value7.getValue(env)
    val valueObject8 = value8.getValue(env)
    val valueObject9 = value9.getValue(env)
    AviatorRuntimeJavaType.valueOf(QueryBuilders.termsQuery(fieldString, valueObject, valueObject2, valueObject3, valueObject4, valueObject5, valueObject6, valueObject7, valueObject8, valueObject9))
  }
  override def call(env: JavaMap[String, AnyRef], field: AviatorObject, value: AviatorObject, value2: AviatorObject, value3: AviatorObject, value4: AviatorObject, value5: AviatorObject, value6: AviatorObject, value7: AviatorObject, value8: AviatorObject, value9: AviatorObject, value10: AviatorObject): AviatorObject = {
    val fieldString = FunctionUtils.getStringValue(field, env)
    val valueObject = value.getValue(env)
    val valueObject2 = value2.getValue(env)
    val valueObject3 = value3.getValue(env)
    val valueObject4 = value4.getValue(env)
    val valueObject5 = value5.getValue(env)
    val valueObject6 = value6.getValue(env)
    val valueObject7 = value7.getValue(env)
    val valueObject8 = value8.getValue(env)
    val valueObject9 = value9.getValue(env)
    val valueObject10 = value10.getValue(env)
    AviatorRuntimeJavaType.valueOf(QueryBuilders.termsQuery(fieldString, valueObject, valueObject2, valueObject3, valueObject4, valueObject5, valueObject6, valueObject7, valueObject8, valueObject9, valueObject10))
  }
  override def call(env: JavaMap[String, AnyRef], field: AviatorObject, value: AviatorObject, value2: AviatorObject, value3: AviatorObject, value4: AviatorObject, value5: AviatorObject, value6: AviatorObject, value7: AviatorObject, value8: AviatorObject, value9: AviatorObject, value10: AviatorObject, value11: AviatorObject): AviatorObject = {
    val fieldString = FunctionUtils.getStringValue(field, env)
    val valueObject = value.getValue(env)
    val valueObject2 = value2.getValue(env)
    val valueObject3 = value3.getValue(env)
    val valueObject4 = value4.getValue(env)
    val valueObject5 = value5.getValue(env)
    val valueObject6 = value6.getValue(env)
    val valueObject7 = value7.getValue(env)
    val valueObject8 = value8.getValue(env)
    val valueObject9 = value9.getValue(env)
    val valueObject10 = value10.getValue(env)
    val valueObject11 = value11.getValue(env)
    AviatorRuntimeJavaType.valueOf(QueryBuilders.termsQuery(fieldString, valueObject, valueObject2, valueObject3, valueObject4, valueObject5, valueObject6, valueObject7, valueObject8, valueObject9, valueObject10, valueObject11))
  }
}

@IFunction
class EsBoolFunction extends AbstractFunction {
  override def getName: String = "es.bool"

  override def call(env: JavaMap[String, AnyRef], value: AviatorObject): AviatorObject = {
    val boolQueryBuilder = QueryBuilders.boolQuery()
    FunctionUtils.getJavaObject(value, env).asInstanceOf[EsElement[BoolQueryBuilder]].create(boolQueryBuilder)
    AviatorRuntimeJavaType.valueOf(boolQueryBuilder)
  }
  override def call(env: JavaMap[String, AnyRef], value: AviatorObject, value2: AviatorObject): AviatorObject = {
    val boolQueryBuilder = QueryBuilders.boolQuery()
    FunctionUtils.getJavaObject(value, env).asInstanceOf[EsElement[BoolQueryBuilder]].create(boolQueryBuilder)
    FunctionUtils.getJavaObject(value2, env).asInstanceOf[EsElement[BoolQueryBuilder]].create(boolQueryBuilder)
    AviatorRuntimeJavaType.valueOf(boolQueryBuilder)
  }
  override def call(env: JavaMap[String, AnyRef], value: AviatorObject, value2: AviatorObject, value3: AviatorObject): AviatorObject = {
    val boolQueryBuilder = QueryBuilders.boolQuery()
    FunctionUtils.getJavaObject(value, env).asInstanceOf[EsElement[BoolQueryBuilder]].create(boolQueryBuilder)
    FunctionUtils.getJavaObject(value2, env).asInstanceOf[EsElement[BoolQueryBuilder]].create(boolQueryBuilder)
    FunctionUtils.getJavaObject(value3, env).asInstanceOf[EsElement[BoolQueryBuilder]].create(boolQueryBuilder)
    AviatorRuntimeJavaType.valueOf(boolQueryBuilder)
  }
  override def call(env: JavaMap[String, AnyRef], value: AviatorObject, value2: AviatorObject, value3: AviatorObject, value4: AviatorObject): AviatorObject = {
    val boolQueryBuilder = QueryBuilders.boolQuery()
    FunctionUtils.getJavaObject(value, env).asInstanceOf[EsElement[BoolQueryBuilder]].create(boolQueryBuilder)
    FunctionUtils.getJavaObject(value2, env).asInstanceOf[EsElement[BoolQueryBuilder]].create(boolQueryBuilder)
    FunctionUtils.getJavaObject(value4, env).asInstanceOf[EsElement[BoolQueryBuilder]].create(boolQueryBuilder)
    AviatorRuntimeJavaType.valueOf(boolQueryBuilder)
  }
}
