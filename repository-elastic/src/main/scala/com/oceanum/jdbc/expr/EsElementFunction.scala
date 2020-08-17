package com.oceanum.jdbc.expr

import com.googlecode.aviator.runtime.`type`.{AviatorObject, AviatorRuntimeJavaType}
import com.googlecode.aviator.runtime.function.AbstractFunction
import com.oceanum.annotation.IFunction
import org.elasticsearch.index.query.{BoolQueryBuilder, QueryBuilder, RangeQueryBuilder}


@IFunction
class EsGtFunction extends AbstractFunction {
  override def getName: String = "es.gt"

  override def call(env: JavaMap[String, AnyRef], value: AviatorObject): AviatorObject = {
    AviatorRuntimeJavaType.valueOf(EsGt(value.getValue(env)))
  }
}

@IFunction
class EsGteFunction extends AbstractFunction {
  override def getName: String = "es.gte"

  override def call(env: JavaMap[String, AnyRef], value: AviatorObject): AviatorObject = {
    AviatorRuntimeJavaType.valueOf(EsGte(value.getValue(env)))
  }
}

@IFunction
class EsLtFunction extends AbstractFunction {
  override def getName: String = "es.lt"

  override def call(env: JavaMap[String, AnyRef], value: AviatorObject): AviatorObject = {
    AviatorRuntimeJavaType.valueOf(EsLt(value.getValue(env)))
  }
}

@IFunction
class EsLteFunction extends AbstractFunction {
  override def getName: String = "es.lte"

  override def call(env: JavaMap[String, AnyRef], value: AviatorObject): AviatorObject = {
    AviatorRuntimeJavaType.valueOf(EsLte(value.getValue(env)))
  }
}
@IFunction
class EsMustFunction extends EsElementFunction("es.must", EsMust.apply)
@IFunction
class EsMustNotFunction extends EsElementFunction("es.mustNot", EsMustNot.apply)
@IFunction
class EsShouldFunction extends EsElementFunction("es.should", EsShould.apply)
@IFunction
class EsFilterFunction extends EsElementFunction("es.filter", EsFilter.apply)


import java.util.{List => JavaList}
import scala.collection.JavaConversions.{collectionAsScalaIterable, seqAsJavaList}
trait EsElement[T<:QueryBuilder] {
  def create(queryBuilder: T)
}
case class EsMust(list: JavaList[QueryBuilder]) extends EsElement[BoolQueryBuilder] {
  override def create(boolQueryBuilder: BoolQueryBuilder): Unit = list.foreach(boolQueryBuilder.must)
}
case class EsMustNot(list: JavaList[QueryBuilder]) extends EsElement[BoolQueryBuilder] {
  override def create(boolQueryBuilder: BoolQueryBuilder): Unit = list.foreach(boolQueryBuilder.mustNot)
}
case class EsShould(list: JavaList[QueryBuilder]) extends EsElement[BoolQueryBuilder] {
  override def create(boolQueryBuilder: BoolQueryBuilder): Unit = list.foreach(boolQueryBuilder.should)
}
case class EsFilter(list: JavaList[QueryBuilder]) extends EsElement[BoolQueryBuilder] {
  override def create(boolQueryBuilder: BoolQueryBuilder): Unit = list.foreach(boolQueryBuilder.filter)
}
case class EsGt(anyRef: AnyRef) extends EsElement[RangeQueryBuilder] {
  override def create(queryBuilder: RangeQueryBuilder): Unit = queryBuilder.gt(anyRef)
}
case class EsGte(anyRef: AnyRef) extends EsElement[RangeQueryBuilder] {
  override def create(queryBuilder: RangeQueryBuilder): Unit = queryBuilder.gte(anyRef)
}
case class EsLt(anyRef: AnyRef) extends EsElement[RangeQueryBuilder] {
  override def create(queryBuilder: RangeQueryBuilder): Unit = queryBuilder.lt(anyRef)
}
case class EsLte(anyRef: AnyRef) extends EsElement[RangeQueryBuilder] {
  override def create(queryBuilder: RangeQueryBuilder): Unit = queryBuilder.lte(anyRef)
}

class EsElementFunction(name: String, func: JavaList[QueryBuilder] => EsElement[BoolQueryBuilder]) extends AbstractFunction {
  override def getName: String = name
  override def call(env: JavaMap[String, AnyRef], value: AviatorObject): AviatorObject = {
    val valueObject = value.getValue(env).asInstanceOf[QueryBuilder]
    AviatorRuntimeJavaType.valueOf(func(Seq(valueObject)))
  }
  override def call(env: JavaMap[String, AnyRef], value: AviatorObject, value1: AviatorObject): AviatorObject = {
    val valueObject = value.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject1 = value1.getValue(env).asInstanceOf[QueryBuilder]
    AviatorRuntimeJavaType.valueOf(func(Seq(valueObject, valueObject1)))
  }
  override def call(env: JavaMap[String, AnyRef], value: AviatorObject, value1: AviatorObject, value2: AviatorObject): AviatorObject = {
    val valueObject = value.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject1 = value1.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject2 = value2.getValue(env).asInstanceOf[QueryBuilder]
    AviatorRuntimeJavaType.valueOf(func(Seq(valueObject, valueObject1, valueObject2)))
  }
  override def call(env: JavaMap[String, AnyRef], value: AviatorObject, value1: AviatorObject, value2: AviatorObject, value3: AviatorObject): AviatorObject = {
    val valueObject = value.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject1 = value1.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject2 = value2.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject3 = value3.getValue(env).asInstanceOf[QueryBuilder]
    AviatorRuntimeJavaType.valueOf(func(Seq(valueObject, valueObject1, valueObject2, valueObject3)))
  }
  override def call(env: JavaMap[String, AnyRef], value: AviatorObject, value1: AviatorObject, value2: AviatorObject, value3: AviatorObject, value4: AviatorObject): AviatorObject = {
    val valueObject = value.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject1 = value1.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject2 = value2.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject3 = value3.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject4 = value4.getValue(env).asInstanceOf[QueryBuilder]
    AviatorRuntimeJavaType.valueOf(func(Seq(valueObject, valueObject1, valueObject2, valueObject3, valueObject4)))
  }
  override def call(env: JavaMap[String, AnyRef], value: AviatorObject, value1: AviatorObject, value2: AviatorObject, value3: AviatorObject, value4: AviatorObject, value5: AviatorObject): AviatorObject = {
    val valueObject = value.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject1 = value1.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject2 = value2.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject3 = value3.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject4 = value4.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject5 = value5.getValue(env).asInstanceOf[QueryBuilder]
    AviatorRuntimeJavaType.valueOf(func(Seq(valueObject, valueObject1, valueObject2, valueObject3, valueObject4, valueObject5)))
  }
  override def call(env: JavaMap[String, AnyRef], value: AviatorObject, value1: AviatorObject, value2: AviatorObject, value3: AviatorObject, value4: AviatorObject, value5: AviatorObject, value6: AviatorObject): AviatorObject = {
    val valueObject = value.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject1 = value1.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject2 = value2.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject3 = value3.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject4 = value4.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject5 = value5.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject6 = value6.getValue(env).asInstanceOf[QueryBuilder]
    AviatorRuntimeJavaType.valueOf(func(Seq(valueObject, valueObject1, valueObject2, valueObject3, valueObject4, valueObject5, valueObject6)))
  }
  override def call(env: JavaMap[String, AnyRef], value: AviatorObject, value1: AviatorObject, value2: AviatorObject, value3: AviatorObject, value4: AviatorObject, value5: AviatorObject, value6: AviatorObject, value7: AviatorObject): AviatorObject = {
    val valueObject = value.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject1 = value1.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject2 = value2.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject3 = value3.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject4 = value4.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject5 = value5.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject6 = value6.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject7 = value7.getValue(env).asInstanceOf[QueryBuilder]
    AviatorRuntimeJavaType.valueOf(func(Seq(valueObject, valueObject1, valueObject2, valueObject3, valueObject4, valueObject5, valueObject6, valueObject7)))
  }
  override def call(env: JavaMap[String, AnyRef], value: AviatorObject, value1: AviatorObject, value2: AviatorObject, value3: AviatorObject, value4: AviatorObject, value5: AviatorObject, value6: AviatorObject, value7: AviatorObject, value8: AviatorObject): AviatorObject = {
    val valueObject = value.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject1 = value1.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject2 = value2.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject3 = value3.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject4 = value4.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject5 = value5.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject6 = value6.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject7 = value7.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject8 = value8.getValue(env).asInstanceOf[QueryBuilder]
    AviatorRuntimeJavaType.valueOf(func(Seq(valueObject, valueObject1, valueObject2, valueObject3, valueObject4, valueObject5, valueObject6, valueObject7, valueObject8)))
  }
  override def call(env: JavaMap[String, AnyRef], value: AviatorObject, value1: AviatorObject, value2: AviatorObject, value3: AviatorObject, value4: AviatorObject, value5: AviatorObject, value6: AviatorObject, value7: AviatorObject, value8: AviatorObject, value9: AviatorObject): AviatorObject = {
    val valueObject = value.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject1 = value1.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject2 = value2.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject3 = value3.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject4 = value4.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject5 = value5.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject6 = value6.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject7 = value7.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject8 = value8.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject9 = value9.getValue(env).asInstanceOf[QueryBuilder]
    AviatorRuntimeJavaType.valueOf(func(Seq(valueObject, valueObject1, valueObject2, valueObject3, valueObject4, valueObject5, valueObject6, valueObject7, valueObject8, valueObject9)))
  }
  override def call(env: JavaMap[String, AnyRef], value: AviatorObject, value1: AviatorObject, value2: AviatorObject, value3: AviatorObject, value4: AviatorObject, value5: AviatorObject, value6: AviatorObject, value7: AviatorObject, value8: AviatorObject, value9: AviatorObject, value10: AviatorObject): AviatorObject = {
    val valueObject = value.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject1 = value1.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject2 = value2.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject3 = value3.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject4 = value4.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject5 = value5.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject6 = value6.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject7 = value7.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject8 = value8.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject9 = value9.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject10 = value10.getValue(env).asInstanceOf[QueryBuilder]
    AviatorRuntimeJavaType.valueOf(func(Seq(valueObject, valueObject1, valueObject2, valueObject3, valueObject4, valueObject5, valueObject6, valueObject7, valueObject8, valueObject9, valueObject10)))
  }
  override def call(env: JavaMap[String, AnyRef], value: AviatorObject, value1: AviatorObject, value2: AviatorObject, value3: AviatorObject, value4: AviatorObject, value5: AviatorObject, value6: AviatorObject, value7: AviatorObject, value8: AviatorObject, value9: AviatorObject, value10: AviatorObject, value11: AviatorObject): AviatorObject = {
    val valueObject = value.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject1 = value1.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject2 = value2.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject3 = value3.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject4 = value4.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject5 = value5.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject6 = value6.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject7 = value7.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject8 = value8.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject9 = value9.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject10 = value10.getValue(env).asInstanceOf[QueryBuilder]
    val valueObject11 = value11.getValue(env).asInstanceOf[QueryBuilder]
    AviatorRuntimeJavaType.valueOf(func(Seq(valueObject, valueObject1, valueObject2, valueObject3, valueObject4, valueObject5, valueObject6, valueObject7, valueObject8, valueObject9, valueObject10, valueObject11)))
  }
}
