package com.oceanum.expr

import com.googlecode.aviator.runtime.`type`.{AviatorObject, AviatorRuntimeJavaType}
import com.googlecode.aviator.runtime.function.{AbstractFunction, FunctionUtils}
import com.oceanum.annotation.IFunction
import org.elasticsearch.search.sort.{SortBuilders, SortMode, SortOrder}

/**
 * @author chenmingkun
 * @date 2020/8/2
 */
@IFunction
class EsSortFunction extends AbstractFunction {
  override def getName: String = "es.sort"

  override def call(env: JavaMap[String, AnyRef], field: AviatorObject): AviatorObject = {
    AviatorRuntimeJavaType.valueOf(SortBuilders.fieldSort(FunctionUtils.getStringValue(field, env)))
  }

  override def call(env: JavaMap[String, AnyRef], field: AviatorObject, order: AviatorObject): AviatorObject = {
    val orderString = SortOrder.valueOf(FunctionUtils.getStringValue(order, env).toUpperCase())
    AviatorRuntimeJavaType.valueOf(SortBuilders.fieldSort(FunctionUtils.getStringValue(field, env)).order(orderString))
  }
}

@IFunction
class EsScoreSortFunction extends AbstractFunction {
  override def getName: String = "es.scoreSort"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = {
    AviatorRuntimeJavaType.valueOf(SortBuilders.scoreSort())
  }
}