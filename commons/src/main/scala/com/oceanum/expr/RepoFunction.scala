package com.oceanum.expr

import com.googlecode.aviator.lexer.token.OperatorType
import com.googlecode.aviator.runtime.`type`.{AviatorObject, AviatorRuntimeJavaType}
import com.googlecode.aviator.runtime.function.{AbstractFunction, FunctionUtils}

abstract class RepoFindByFunction extends AbstractFunction {
  override def getName: String = "repo.field"

  override def call(env: JavaMap[String, AnyRef], field: AviatorObject, value: AviatorObject): AviatorObject = {
    val fieldString = FunctionUtils.getStringValue(field, env)
    val valueObject = value.getValue(env)
    call(fieldString, valueObject)
  }

  def call(field: String, value: Object): AviatorObject
}

abstract class RepoSortFunction extends AbstractFunction {
  override def getName: String = "repo.sort"

  override def call(env: JavaMap[String, AnyRef], field: AviatorObject): AviatorObject = {
    val fieldString = FunctionUtils.getStringValue(field, env)
    call(fieldString)
  }

  override def call(env: JavaMap[String, AnyRef], field: AviatorObject, sortType: AviatorObject): AviatorObject = {
    val fieldString = FunctionUtils.getStringValue(field, env)
    val sortString = FunctionUtils.getStringValue(sortType, env)
    call(fieldString, sortString)
  }

  def call(field: String): AviatorObject

  def call(field: String, sortType: String): AviatorObject
}
//
//class RepoSortsFunction extends AbstractFunction {
//  override def getName: String = "repo.sorts"
//
//  override def call(env: JavaMap[String, AnyRef], sorts: AviatorObject): AviatorObject = {
//    val fieldString = FunctionUtils.getStringValue(sorts, env)
//    val arr = fieldString.split(",")
//      .map(_.trim)
//      .map(_.split(":"))
//      .map(arr =>
//        if (arr.length == 1) Evaluator.rawExecute(s"repo.sort(${arr(0)})", new JavaHashMap(0))
//        else Evaluator.rawExecute(s"repo.sort(${arr(0)}, ${arr(1)})", new JavaHashMap(0))
//      )
//    AviatorRuntimeJavaType.valueOf(arr)
//  }
//}

abstract class RepoLimitFunction extends AbstractFunction {
  override def getName: String = "repo.limit"

  override def call(env: JavaMap[String, AnyRef], limit: AviatorObject): AviatorObject = {
    val limitNum = FunctionUtils.getNumberValue(limit, env)
    call(limitNum.intValue())
  }

  def call(limit: Int): AviatorObject
}

abstract class RepoSelectFunction extends AbstractFunction {
  override def getName: String = "repo.select"

  override def call(env: JavaMap[String, AnyRef], elem: AviatorObject): AviatorObject = {
    val obj = FunctionUtils.getJavaObject(elem, env)
    call(obj)
  }
  override def call(env: JavaMap[String, AnyRef], elem: AviatorObject, elem2: AviatorObject): AviatorObject = {
    val obj = FunctionUtils.getJavaObject(elem, env)
    val obj2 = FunctionUtils.getJavaObject(elem2, env)
    call(obj, obj2)
  }
  override def call(env: JavaMap[String, AnyRef], elem: AviatorObject, elem2: AviatorObject, elem3: AviatorObject): AviatorObject = {
    val obj = FunctionUtils.getJavaObject(elem, env)
    val obj2 = FunctionUtils.getJavaObject(elem2, env)
    val obj3 = FunctionUtils.getJavaObject(elem3, env)
    call(obj, obj2, obj3)
  }
  override def call(env: JavaMap[String, AnyRef], elem: AviatorObject, elem2: AviatorObject, elem3: AviatorObject, elem4: AviatorObject): AviatorObject = {
    val obj = FunctionUtils.getJavaObject(elem, env)
    val obj2 = FunctionUtils.getJavaObject(elem2, env)
    val obj3 = FunctionUtils.getJavaObject(elem3, env)
    val obj4 = FunctionUtils.getJavaObject(elem4, env)
    call(obj, obj2, obj3, obj4)
  }
  override def call(env: JavaMap[String, AnyRef], elem: AviatorObject, elem2: AviatorObject, elem3: AviatorObject, elem4: AviatorObject, elem5: AviatorObject): AviatorObject = {
    val obj = FunctionUtils.getJavaObject(elem, env)
    val obj2 = FunctionUtils.getJavaObject(elem2, env)
    val obj3 = FunctionUtils.getJavaObject(elem3, env)
    val obj4 = FunctionUtils.getJavaObject(elem4, env)
    val obj5 = FunctionUtils.getJavaObject(elem5, env)
    call(obj, obj2, obj3, obj4, obj5)
  }
  def call(elem: AnyRef): AviatorObject
  def call(elem: AnyRef, elem2: AnyRef): AviatorObject
  def call(elem: AnyRef, elem2: AnyRef, elem3: AnyRef): AviatorObject
  def call(elem: AnyRef, elem2: AnyRef, elem3: AnyRef, elem4: AnyRef): AviatorObject
  def call(elem: AnyRef, elem2: AnyRef, elem3: AnyRef, elem4: AnyRef, elem5: AnyRef): AviatorObject
  def call(elem: AnyRef, elem2: AnyRef, elem3: AnyRef, elem4: AnyRef, elem5: AnyRef, elem6: AnyRef): AviatorObject
  def call(elem: AnyRef, elem2: AnyRef, elem3: AnyRef, elem4: AnyRef, elem5: AnyRef, elem6: AnyRef, elem7: AnyRef): AviatorObject
}

abstract class RepoAndFunction extends AbstractFunction {
  override def getName: String = OperatorType.AND.token
}

abstract class RepoOrFunction extends AbstractFunction {
  override def getName: String = OperatorType.OR.token
}

abstract class RepoNotFunction extends AbstractFunction {
  override def getName: String = OperatorType.NOT.token
}