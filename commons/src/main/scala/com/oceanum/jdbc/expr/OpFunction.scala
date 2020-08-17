package com.oceanum.jdbc.expr

import java.util.Date
import java.util.concurrent.TimeUnit

import com.googlecode.aviator.lexer.token.OperatorType
import com.googlecode.aviator.runtime.`type`.{AviatorFunction, AviatorObject, AviatorRuntimeJavaType}
import com.googlecode.aviator.runtime.function.AbstractFunction
import com.oceanum.annotation.IOpFunction

import scala.concurrent.duration.Duration

trait OpFunction {
  def getOpType: OperatorType
  def call1(env: JavaMap[String, AnyRef]): PartialFunction[Any, AviatorObject] = PartialFunction.empty
  def call2(env: JavaMap[String, AnyRef]): PartialFunction[(Any, Any), AviatorObject] = PartialFunction.empty

  def merge(opFunction: OpFunction): OpFunction = {
    val func1: JavaMap[String, AnyRef] => PartialFunction[Any, AviatorObject] = this.call1
    val func2: JavaMap[String, AnyRef] => PartialFunction[(Any, Any), AviatorObject] = this.call2
    if (getOpType == opFunction.getOpType) {
      new OpFunction {
        override def getOpType: OperatorType = opFunction.getOpType
        override def call1(env: JavaMap[String, AnyRef]): PartialFunction[Any, AviatorObject] = func1(env).orElse(opFunction.call1(env))
        override def call2(env: JavaMap[String, AnyRef]): PartialFunction[(Any, Any), AviatorObject] = func2(env).orElse(opFunction.call2(env))
      }
    } else {
      throw new IllegalArgumentException("wrong op type")
    }
  }

  def toAviator: AviatorFunction = new AbstractFunction {
    override def getName: String = getOpType.token
    override def call(env: JavaMap[String, AnyRef], arg1: AviatorObject): AviatorObject = {
      val obj = arg1.getValue(env)
      call1(env)
        .orElse[Any, AviatorObject] {
          case _ => getOpType.eval(Array(arg1), env)
        }
        .apply(obj)
    }
    override def call(env: JavaMap[String, AnyRef], arg1: AviatorObject, arg2: AviatorObject): AviatorObject = {
      val obj1 = arg1.getValue(env)
      val obj2 = arg2.getValue(env)
      call2(env)
        .orElse[(Any, Any), AviatorObject] {
          case (_, _) => getOpType.eval(Array(arg1, arg2), env)
        }
        .apply(obj1, obj2)
    }
  }
}

object OpFunction {
  def empty(operatorType: OperatorType): OpFunction = new OpFunction {
    override def getOpType: OperatorType = operatorType
  }
}

@IOpFunction
class OpAdd extends OpFunction {
  override def getOpType: OperatorType = OperatorType.ADD

  override def call2(env: JavaMap[String, AnyRef]): PartialFunction[(Any, Any), AviatorObject] = {
    case (left: Date, right: Duration) => AviatorRuntimeJavaType.valueOf(new Date(left.getTime + right.toMillis))
    case (left: Duration, right: Date) => AviatorRuntimeJavaType.valueOf(new Date(left.toMillis + right.getTime))
    case (left: Duration, right: Duration) => AviatorRuntimeJavaType.valueOf(left + right)
  }
}

@IOpFunction
class OpSub extends OpFunction {
  override def getOpType: OperatorType = OperatorType.SUB

  override def call2(env: JavaMap[String, AnyRef]): PartialFunction[(Any, Any), AviatorObject] = {
    case (left: Date, right: Duration) => AviatorRuntimeJavaType.valueOf(new Date(left.getTime - right.toMillis))
    case (left: Date, right: Date) => AviatorRuntimeJavaType.valueOf(Duration(left.getTime - right.getTime, TimeUnit.MILLISECONDS))
    case (left: Duration, right: Duration) => AviatorRuntimeJavaType.valueOf(left - right)
  }
}

@IOpFunction
class OpNeg extends OpFunction {
  override def getOpType: OperatorType = OperatorType.NEG

  override def call1(env: JavaMap[String, AnyRef]): PartialFunction[Any, AviatorObject] = {
    case d: Duration => AviatorRuntimeJavaType.valueOf(d.neg())
  }
}

@IOpFunction
class OpDiv extends OpFunction {
  override def getOpType: OperatorType = OperatorType.DIV

  override def call2(env: JavaMap[String, AnyRef]): PartialFunction[(Any, Any), AviatorObject] = {
    case (left: Duration, right: Duration) => AviatorRuntimeJavaType.valueOf(left / right)
    case (left: Duration, right: Double) => AviatorRuntimeJavaType.valueOf(left / right)
    case (left: Duration, right: Float) => AviatorRuntimeJavaType.valueOf(left / right)
    case (left: Duration, right: Int) => AviatorRuntimeJavaType.valueOf(left / right)
    case (left: Duration, right: Long) => AviatorRuntimeJavaType.valueOf(left / right)
    case (left: Duration, right: Short) => AviatorRuntimeJavaType.valueOf(left / right)
    case (left: Duration, right: Number) => AviatorRuntimeJavaType.valueOf(left / right.doubleValue())
  }
}

@IOpFunction
class OpMulti extends OpFunction {
  override def getOpType: OperatorType = OperatorType.MULT

  override def call2(env: JavaMap[String, AnyRef]): PartialFunction[(Any, Any), AviatorObject] = {
    case (left: Duration, right: Double) => AviatorRuntimeJavaType.valueOf(left * right)
    case (left: Duration, right: Float) => AviatorRuntimeJavaType.valueOf(left * right)
    case (left: Duration, right: Int) => AviatorRuntimeJavaType.valueOf(left * right)
    case (left: Duration, right: Long) => AviatorRuntimeJavaType.valueOf(left * right)
    case (left: Duration, right: Short) => AviatorRuntimeJavaType.valueOf(left * right)
    case (left: Duration, right: Number) => AviatorRuntimeJavaType.valueOf(left * right.doubleValue())
  }
}

@IOpFunction
class OpGt extends OpFunction {
  override def getOpType: OperatorType = OperatorType.GT

  override def call2(env: JavaMap[String, AnyRef]): PartialFunction[(Any, Any), AviatorObject] = {
    case (left: Duration, right: Duration) => AviatorRuntimeJavaType.valueOf(left > right)
  }
}