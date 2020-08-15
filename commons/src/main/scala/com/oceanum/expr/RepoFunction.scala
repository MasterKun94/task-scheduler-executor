package com.oceanum.expr

import java.util

import com.googlecode.aviator.runtime.`type`.{AviatorObject, AviatorRuntimeJavaType, AviatorType}
import com.googlecode.aviator.runtime.function.{AbstractFunction, FunctionUtils}
import com.oceanum.annotation.IFunction
import com.oceanum.api.Sort

@IFunction
class RepoFieldFunction extends AbstractFunction {
  override def getName: String = "repo.field"

  override def call(env: JavaMap[String, AnyRef], field: AviatorObject): AviatorObject = {
    val fieldString = FunctionUtils.getStringValue(field, env)
    AviatorRuntimeJavaType.valueOf(RepoField(fieldString))
  }
}

abstract class RepoNoneFunction extends AbstractFunction {
  override def getName: String = "repo.findAll"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = {
    emptyCall()
  }

  def emptyCall(): AviatorObject
}

abstract class RepoTermFunction extends AbstractFunction {
  override def getName: String = "repo.term"

  override def call(env: JavaMap[String, AnyRef], field: AviatorObject, value: AviatorObject): AviatorObject = {
    val fieldString = FunctionUtils.getStringValue(field, env)
    val valueObject = value.getValue(env)
    call(fieldString, valueObject)
  }

  def call(field: String, value: Object): AviatorObject
}

abstract class RepoTermsFunction extends AbstractFunction {
  import scala.collection.JavaConversions._
  override def getName: String = "repo.terms"

  override def call(env: JavaMap[String, AnyRef], field: AviatorObject, value: AviatorObject): AviatorObject = {
    val fieldString = FunctionUtils.getStringValue(field, env)
    val valueObject: util.Collection[_] = value.getValue(env) match {
      case collection: util.Collection[_] => collection
      case traversable: TraversableOnce[_] => traversable.toSeq
    }
    call(fieldString, valueObject)
  }

  def call(field: String, value: java.util.Collection[_]): AviatorObject
}

abstract class RepoFieldExistsFunction extends AbstractFunction {
  override def getName: String = "repo.exists"

  override def call(env: JavaMap[String, AnyRef], field: AviatorObject): AviatorObject = {
    val fieldString = FunctionUtils.getStringValue(field, env)
    call(fieldString)
  }

  def call(field: String): AviatorObject
}

abstract class RepoSortFunction extends AbstractFunction {
  override def getName: String = "repo.sort"

  override def call(env: JavaMap[String, AnyRef], field: AviatorObject): AviatorObject = {
    field.getAviatorType match {
      case AviatorType.JavaType =>
        val sorts = FunctionUtils.getJavaObject(field, env).asInstanceOf[Array[Sort]]
        call(sorts)

      case _ =>
        val fieldString = FunctionUtils.getStringValue(field, env)
        call(fieldString)
    }
  }

  override def call(env: JavaMap[String, AnyRef], field: AviatorObject, sortType: AviatorObject): AviatorObject = {
    val fieldString = FunctionUtils.getStringValue(field, env)
    val sortString = FunctionUtils.getStringValue(sortType, env)
    call(fieldString, sortString)
  }

  def call(field: String): AviatorObject

  def call(field: String, sortType: String): AviatorObject

  def call(sorts: Array[Sort]): AviatorObject
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

abstract class RepoSizeFunction extends AbstractFunction {
  override def getName: String = "repo.size"

  override def call(env: JavaMap[String, AnyRef], limit: AviatorObject): AviatorObject = {
    val limitNum = FunctionUtils.getNumberValue(limit, env)
    call(limitNum.intValue())
  }

  def call(limit: Int): AviatorObject
}

abstract class RepoPageFunction extends AbstractFunction {
  override def getName: String = "repo.page"

  override def call(env: JavaMap[String, AnyRef], page: AviatorObject): AviatorObject = {
    val pageNum = FunctionUtils.getNumberValue(page, env)
    call(pageNum.intValue())
  }

  def call(page: Int): AviatorObject
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

case class RepoField(field: String)