package com.oceanum.expr

import java.util

import com.googlecode.aviator.lexer.token.OperatorType
import com.googlecode.aviator.runtime.`type`.{AviatorObject, AviatorRuntimeJavaType, AviatorType}
import com.googlecode.aviator.runtime.function.{AbstractFunction, FunctionUtils}
import com.oceanum.annotation.{IFunction, IOpFunction}
import com.oceanum.api.Sort
import com.oceanum.persistence.Expression

@IFunction
class RepoFieldFunction extends AbstractFunction {
  override def getName: String = "repo.field"

  override def call(env: JavaMap[String, AnyRef], field: AviatorObject): AviatorObject = {
    val fieldString = FunctionUtils.getStringValue(field, env)
    AviatorRuntimeJavaType.valueOf(RepoField(fieldString))
  }
}

@IFunction
class RepoNoneFunction extends AbstractFunction {
  override def getName: String = "repo.findAll"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = {
    emptyCall()
  }

  def emptyCall(): AviatorObject = {
    AviatorRuntimeJavaType.valueOf(exprFactory.findAll())
  }
}

@IFunction
class RepoTermFunction extends AbstractFunction {
  override def getName: String = "repo.term"

  override def call(env: JavaMap[String, AnyRef], field: AviatorObject, value: AviatorObject): AviatorObject = {
    val fieldString = FunctionUtils.getStringValue(field, env)
    val valueObject = value.getValue(env)
    call(fieldString, valueObject)
  }

  def call(field: String, value: AnyRef): AviatorObject = {
    AviatorRuntimeJavaType.valueOf(exprFactory.term(field, value))
  }
}

@IFunction
class RepoTermsFunction extends AbstractFunction {
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

  def call(field: String, value: java.util.Collection[_]): AviatorObject = {
    AviatorRuntimeJavaType.valueOf(exprFactory.terms(field, value.toArray))
  }
}

@IFunction
class RepoFieldExistsFunction extends AbstractFunction {
  override def getName: String = "repo.exists"

  override def call(env: JavaMap[String, AnyRef], field: AviatorObject): AviatorObject = {
    val fieldString = FunctionUtils.getStringValue(field, env)
    call(fieldString)
  }

  def call(field: String): AviatorObject = {
    AviatorRuntimeJavaType.valueOf(exprFactory.exists(field))
  }
}

@IFunction
class RepoSortFunction extends AbstractFunction {
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

  def call(field: String): AviatorObject = {
    call(field, "DESC")
  }

  def call(field: String, sortType: String): AviatorObject = {
    AviatorRuntimeJavaType.valueOf(exprFactory.sort(field, sortType))
  }

  def call(sorts: Array[Sort]): AviatorObject = {
    AviatorRuntimeJavaType.valueOf(exprFactory.sorts(sorts))
  }
}

@IFunction
class RepoSizeFunction extends AbstractFunction {
  override def getName: String = "repo.size"

  override def call(env: JavaMap[String, AnyRef], limit: AviatorObject): AviatorObject = {
    val limitNum = FunctionUtils.getNumberValue(limit, env)
    call(limitNum.intValue())
  }

  def call(limit: Int): AviatorObject = {
    AviatorRuntimeJavaType.valueOf(exprFactory.size(limit))
  }
}

@IFunction
class RepoPageFunction extends AbstractFunction {
  override def getName: String = "repo.page"

  override def call(env: JavaMap[String, AnyRef], page: AviatorObject): AviatorObject = {
    val pageNum = FunctionUtils.getNumberValue(page, env)
    AviatorRuntimeJavaType.valueOf(exprFactory.page(pageNum.intValue()))
  }
}

@IFunction
class RepoSelectFunction extends AbstractFunction {
  override def getName: String = "repo.select"

  override def call(env: JavaMap[String, AnyRef], elem: AviatorObject, elem2: AviatorObject): AviatorObject = {
    val obj = FunctionUtils.getJavaObject(elem, env)
    val obj2 = FunctionUtils.getJavaObject(elem2, env)
    call(obj.asInstanceOf[Expression], obj2.asInstanceOf[Expression])
  }
  override def call(env: JavaMap[String, AnyRef], elem: AviatorObject, elem2: AviatorObject, elem3: AviatorObject): AviatorObject = {
    val obj = FunctionUtils.getJavaObject(elem, env)
    val obj2 = FunctionUtils.getJavaObject(elem2, env)
    val obj3 = FunctionUtils.getJavaObject(elem3, env)
    call(obj.asInstanceOf[Expression], obj2.asInstanceOf[Expression], obj3.asInstanceOf[Expression])
  }
  override def call(env: JavaMap[String, AnyRef], elem: AviatorObject, elem2: AviatorObject, elem3: AviatorObject, elem4: AviatorObject): AviatorObject = {
    val obj = FunctionUtils.getJavaObject(elem, env)
    val obj2 = FunctionUtils.getJavaObject(elem2, env)
    val obj3 = FunctionUtils.getJavaObject(elem3, env)
    val obj4 = FunctionUtils.getJavaObject(elem4, env)
    call(obj.asInstanceOf[Expression], obj2.asInstanceOf[Expression], obj3.asInstanceOf[Expression], obj4.asInstanceOf[Expression])
  }

  def call(elem: Expression, elem2: Expression): AviatorObject = {
    AviatorRuntimeJavaType.valueOf(exprFactory.select(elem, elem2))
  }
  def call(elem: Expression, elem2: Expression, elem3: Expression): AviatorObject = {
    AviatorRuntimeJavaType.valueOf(exprFactory.select(elem, elem2, elem3))
  }
  def call(elem: Expression, elem2: Expression, elem3: Expression, elem4: Expression): AviatorObject = {
    AviatorRuntimeJavaType.valueOf(exprFactory.select(elem, elem2, elem3, elem4))
  }
}

case class RepoField(field: String)


@IOpFunction
class EsRepoAndFunction extends OpFunction {
  override def getOpType: OperatorType = OperatorType.AND
  override def call2(env: JavaMap[String, AnyRef]): PartialFunction[(Any, Any), AviatorObject] = {
    case (b1: Expression, b2: Expression) =>
      AviatorRuntimeJavaType.valueOf(exprFactory.and(b1, b2))
  }
}

@IOpFunction
class EsRepoOrFunction extends OpFunction {
  override def getOpType: OperatorType = OperatorType.OR
  override def call2(env: JavaMap[String, AnyRef]): PartialFunction[(Any, Any), AviatorObject] = {
    case (b1: Expression, b2: Expression) =>
      AviatorRuntimeJavaType.valueOf(exprFactory.or(b1, b2))
  }
}

@IOpFunction
class EsRepoNotFunction extends OpFunction {
  override def getOpType: OperatorType = OperatorType.NOT
  override def call1(env: JavaMap[String, AnyRef]): PartialFunction[Any, AviatorObject] = {
    case b1: Expression => AviatorRuntimeJavaType.valueOf(exprFactory.not(b1))
  }
}

@IOpFunction
class EsRepoEqFunction extends OpFunction {
  override def getOpType: OperatorType = OperatorType.EQ
  override def call2(env: JavaMap[String, AnyRef]): PartialFunction[(Any, Any), AviatorObject] = {
    case (b1: RepoField, b2) =>
      AviatorRuntimeJavaType.valueOf(exprFactory.eq(b1.field, b2.asInstanceOf[AnyRef]))
    case (b1, b2: RepoField) =>
      AviatorRuntimeJavaType.valueOf(exprFactory.eq(b2.field, b1.asInstanceOf[AnyRef]))
  }
}

@IOpFunction
class EsRepoNeqFunction extends OpFunction {
  override def getOpType: OperatorType = OperatorType.NEQ
  override def call2(env: JavaMap[String, AnyRef]): PartialFunction[(Any, Any), AviatorObject] = {
    case (b1: RepoField, b2) =>
      AviatorRuntimeJavaType.valueOf(exprFactory.neq(b1.field, b2.asInstanceOf[AnyRef]))
    case (b1, b2: RepoField) =>
      AviatorRuntimeJavaType.valueOf(exprFactory.neq(b2.field, b1.asInstanceOf[AnyRef]))
  }
}

@IOpFunction
class EsRepoMatchFunction extends OpFunction {
  override def getOpType: OperatorType = OperatorType.MATCH
  override def call2(env: JavaMap[String, AnyRef]): PartialFunction[(Any, Any), AviatorObject] = {
    case (b1: RepoField, b2) =>
      AviatorRuntimeJavaType.valueOf(exprFactory.`match`(b1.field, b2.asInstanceOf[AnyRef]))
    case (b1, b2: RepoField) =>
      AviatorRuntimeJavaType.valueOf(exprFactory.`match`(b2.field, b1.asInstanceOf[AnyRef]))
  }
}

@IOpFunction
class EsRepoLtFunction extends OpFunction {
  override def getOpType: OperatorType = OperatorType.LT
  override def call2(env: JavaMap[String, AnyRef]): PartialFunction[(Any, Any), AviatorObject] = {
    case (b1: RepoField, b2) =>
      AviatorRuntimeJavaType.valueOf(exprFactory.lt(b1.field, b2.asInstanceOf[AnyRef]))
    case (b1, b2: RepoField) =>
      AviatorRuntimeJavaType.valueOf(exprFactory.lt(b2.field, b1.asInstanceOf[AnyRef]))
    case (b1: Expression, b2) =>
      AviatorRuntimeJavaType.valueOf(exprFactory.lt(b1, b2.asInstanceOf[AnyRef]))
    case (b1, b2: Expression) =>
      AviatorRuntimeJavaType.valueOf(exprFactory.lt(b2, b1.asInstanceOf[AnyRef]))
  }
}

@IOpFunction
class EsRepoLeFunction extends OpFunction {
  override def getOpType: OperatorType = OperatorType.LE
  override def call2(env: JavaMap[String, AnyRef]): PartialFunction[(Any, Any), AviatorObject] = {
    case (b1: RepoField, b2) =>
      AviatorRuntimeJavaType.valueOf(exprFactory.le(b1.field, b2.asInstanceOf[AnyRef]))
    case (b1, b2: RepoField) =>
      AviatorRuntimeJavaType.valueOf(exprFactory.le(b2.field, b1.asInstanceOf[AnyRef]))
    case (b1: Expression, b2) =>
      AviatorRuntimeJavaType.valueOf(exprFactory.le(b1, b2.asInstanceOf[AnyRef]))
    case (b1, b2: Expression) =>
      AviatorRuntimeJavaType.valueOf(exprFactory.le(b2, b1.asInstanceOf[AnyRef]))
  }
}

@IOpFunction
class EsRepoGtFunction extends OpFunction {
  override def getOpType: OperatorType = OperatorType.GT
  override def call2(env: JavaMap[String, AnyRef]): PartialFunction[(Any, Any), AviatorObject] = {
    case (b1: RepoField, b2) =>
      AviatorRuntimeJavaType.valueOf(exprFactory.gt(b1.field, b2.asInstanceOf[AnyRef]))
    case (b1, b2: RepoField) =>
      AviatorRuntimeJavaType.valueOf(exprFactory.gt(b2.field, b1.asInstanceOf[AnyRef]))
    case (b1: Expression, b2) =>
      AviatorRuntimeJavaType.valueOf(exprFactory.gt(b1, b2.asInstanceOf[AnyRef]))
    case (b1, b2: Expression) =>
      AviatorRuntimeJavaType.valueOf(exprFactory.gt(b2, b1.asInstanceOf[AnyRef]))
  }
}

@IOpFunction
class EsRepoGeFunction extends OpFunction {
  override def getOpType: OperatorType = OperatorType.GE
  override def call2(env: JavaMap[String, AnyRef]): PartialFunction[(Any, Any), AviatorObject] = {
    case (b1: RepoField, b2) =>
      AviatorRuntimeJavaType.valueOf(exprFactory.ge(b1.field, b2.asInstanceOf[AnyRef]))
    case (b1, b2: RepoField) =>
      AviatorRuntimeJavaType.valueOf(exprFactory.ge(b2.field, b1.asInstanceOf[AnyRef]))
    case (b1: Expression, b2) =>
      AviatorRuntimeJavaType.valueOf(exprFactory.ge(b1, b2.asInstanceOf[AnyRef]))
    case (b1, b2: Expression) =>
      AviatorRuntimeJavaType.valueOf(exprFactory.ge(b2, b1.asInstanceOf[AnyRef]))
  }
}