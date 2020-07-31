package com.oceanum.expr

import java.util.Date
import java.util.concurrent.TimeUnit

import com.googlecode.aviator.lexer.token.OperatorType
import com.googlecode.aviator.runtime.`type`.{AviatorBoolean, AviatorDouble, AviatorObject, AviatorRuntimeJavaType, AviatorType}
import com.googlecode.aviator.runtime.function.{AbstractFunction, FunctionUtils}
import com.oceanum.annotation.IOpFunction

import scala.concurrent.duration.Duration

/**
 * @author chenmingkun
 * @date 2020/7/19
 */
@IOpFunction(OperatorType.ADD)
class OpAddFunction extends AbstractFunction {
  override def getName: String = OperatorType.ADD.getToken

  override def call(env: JavaMap[String, AnyRef], arg1: AviatorObject, arg2: AviatorObject): AviatorObject = {
    if (arg1.getAviatorType == AviatorType.JavaType) {
      FunctionUtils.getJavaObject(arg1, env) match {
        case date: Date =>
          if (arg2.getAviatorType == AviatorType.JavaType) {
            FunctionUtils.getJavaObject(arg2, env) match {
              case duration: Duration =>
                return AviatorRuntimeJavaType.valueOf(new Date(date.getTime + duration.toMillis))
              case _ =>
            }
          }
        case duration: Duration =>
          if (arg2.getAviatorType == AviatorType.JavaType) {
            FunctionUtils.getJavaObject(arg2, env) match {
              case dur: Duration =>
                return AviatorRuntimeJavaType.valueOf(dur + duration)
              case date: Date =>
                return AviatorRuntimeJavaType.valueOf(new Date(date.getTime + duration.toMillis))
              case _ =>
            }
          }
        case _ =>
      }
    }
    arg1.add(arg2, env)
  }
}

@IOpFunction(OperatorType.SUB)
class OpSubFunction extends AbstractFunction {
  override def getName: String = OperatorType.SUB.getToken

  override def call(env: JavaMap[String, AnyRef], arg1: AviatorObject, arg2: AviatorObject): AviatorObject = {
    if (arg1.getAviatorType == AviatorType.JavaType) {
      FunctionUtils.getJavaObject(arg1, env) match {
        case date: Date =>
          if (arg2.getAviatorType == AviatorType.JavaType) {
            FunctionUtils.getJavaObject(arg2, env) match {
              case duration: Duration =>
                return AviatorRuntimeJavaType.valueOf(new Date(date.getTime - duration.toMillis))
              case date0: Date =>
                return AviatorRuntimeJavaType.valueOf(Duration(date.getTime - date0.getTime, TimeUnit.MILLISECONDS))
              case _ =>
            }
          }
        case duration: Duration =>
          if (arg2.getAviatorType == AviatorType.JavaType) {
            FunctionUtils.getJavaObject(arg2, env) match {
              case dur: Duration =>
                return AviatorRuntimeJavaType.valueOf(dur - duration)
              case _ =>
            }
          }
        case _ =>
      }
    }
    arg1.sub(arg2, env)
  }
}

@IOpFunction(OperatorType.NEG)
class OpNegFunction extends AbstractFunction {
  override def getName: String = OperatorType.NEG.getToken

  override def call(env: JavaMap[String, AnyRef], arg1: AviatorObject): AviatorObject = {
    if (arg1.getAviatorType == AviatorType.JavaType) {
      FunctionUtils.getJavaObject(arg1, env) match {
        case duration: Duration =>
          return AviatorRuntimeJavaType.valueOf(duration.neg())
        case _ =>
      }
    }
    arg1.neg( env)

  }
}

@IOpFunction(OperatorType.DIV)
class OpDivFunction extends AbstractFunction {
  override def getName: String = OperatorType.DIV.getToken

  override def call(env: JavaMap[String, AnyRef], arg1: AviatorObject, arg2: AviatorObject): AviatorObject = {
    if (arg1.getAviatorType == AviatorType.JavaType) {
      FunctionUtils.getJavaObject(arg1, env) match {
        case left: Duration =>
          arg2.getAviatorType match {
            case AviatorType.JavaType =>
              FunctionUtils.getJavaObject(arg2, env) match {
                case right: Duration =>
                  return AviatorDouble.valueOf(left / right)
                case _ =>
              }
            case AviatorType.Double | AviatorType.Long | AviatorType.Decimal | AviatorType.BigInt =>
              return AviatorRuntimeJavaType.valueOf(left / FunctionUtils.getNumberValue(arg2, env).doubleValue())
            case _ =>
          }
        case _ =>
      }
    }
    arg1.neg( env)
  }
}

@IOpFunction(OperatorType.MULT)
class OpMulFunction extends AbstractFunction {
  override def getName: String = OperatorType.MULT.getToken

  override def call(env: JavaMap[String, AnyRef], arg1: AviatorObject, arg2: AviatorObject): AviatorObject = {
    if (arg1.getAviatorType == AviatorType.JavaType) {
      FunctionUtils.getJavaObject(arg1, env) match {
        case left: Duration =>
          arg2.getAviatorType match {
            case AviatorType.Double | AviatorType.Long | AviatorType.Decimal | AviatorType.BigInt =>
              return AviatorRuntimeJavaType.valueOf(left * FunctionUtils.getNumberValue(arg2, env).doubleValue())
            case _ =>
          }
        case _ =>
      }
    }
    arg1.neg( env)
  }
}

@IOpFunction(OperatorType.GT)
class OpGtFunction extends AbstractFunction {
  override def getName: String = OperatorType.GT.getToken

  override def call(env: JavaMap[String, AnyRef], arg1: AviatorObject, arg2: AviatorObject): AviatorObject = {
    if (arg1.getAviatorType == AviatorType.JavaType) {
      FunctionUtils.getJavaObject(arg1, env) match {
        case left: Duration =>
          arg2.getAviatorType match {
            case AviatorType.JavaType =>
              FunctionUtils.getJavaObject(arg2, env) match {
                case right: Duration =>
                  return AviatorRuntimeJavaType.valueOf(left > right)
                case _ =>
              }
            case _ =>
          }
        case left: Date =>
          arg2.getAviatorType match {
            case AviatorType.JavaType =>
              FunctionUtils.getJavaObject(arg2, env) match {
                case right: Date =>
                  return AviatorRuntimeJavaType.valueOf(left.getTime > right.getTime)
                case _ =>
              }
            case AviatorType.Long =>
              return AviatorRuntimeJavaType.valueOf(left.getTime > FunctionUtils.getNumberValue(arg2, env).longValue())
            case _ =>
          }
        case _ =>
      }
    }
    AviatorBoolean.valueOf(arg1.compare(arg2, env) > 0)
  }
}

@IOpFunction(OperatorType.GE)
class OpGeFunction extends AbstractFunction {
  override def getName: String = OperatorType.GE.getToken

  override def call(env: JavaMap[String, AnyRef], arg1: AviatorObject, arg2: AviatorObject): AviatorObject = {
    if (arg1.getAviatorType == AviatorType.JavaType) {
      FunctionUtils.getJavaObject(arg1, env) match {
        case left: Duration =>
          arg2.getAviatorType match {
            case AviatorType.JavaType =>
              FunctionUtils.getJavaObject(arg2, env) match {
                case right: Duration =>
                  return AviatorRuntimeJavaType.valueOf(left >= right)
                case _ =>
              }
            case _ =>
          }
        case left: Date =>
          arg2.getAviatorType match {
            case AviatorType.JavaType =>
              FunctionUtils.getJavaObject(arg2, env) match {
                case right: Date =>
                  return AviatorRuntimeJavaType.valueOf(left.getTime >= right.getTime)
                case _ =>
              }
            case AviatorType.Long =>
              return AviatorRuntimeJavaType.valueOf(left.getTime >= FunctionUtils.getNumberValue(arg2, env).longValue())
            case _ =>
          }
        case _ =>
      }
    }
    AviatorBoolean.valueOf(arg1.compare(arg2, env) >= 0)
  }
}

@IOpFunction(OperatorType.LT)
class OpLtFunction extends AbstractFunction {
  override def getName: String = OperatorType.LT.getToken

  override def call(env: JavaMap[String, AnyRef], arg1: AviatorObject, arg2: AviatorObject): AviatorObject = {
    if (arg1.getAviatorType == AviatorType.JavaType) {
      FunctionUtils.getJavaObject(arg1, env) match {
        case left: Duration =>
          arg2.getAviatorType match {
            case AviatorType.JavaType =>
              FunctionUtils.getJavaObject(arg2, env) match {
                case right: Duration =>
                  return AviatorRuntimeJavaType.valueOf(left < right)
                case _ =>
              }
            case _ =>
          }
        case left: Date =>
          arg2.getAviatorType match {
            case AviatorType.JavaType =>
              FunctionUtils.getJavaObject(arg2, env) match {
                case right: Date =>
                  return AviatorRuntimeJavaType.valueOf(left.getTime < right.getTime)
                case _ =>
              }
            case AviatorType.Long =>
              return AviatorRuntimeJavaType.valueOf(left.getTime < FunctionUtils.getNumberValue(arg2, env).longValue())
            case _ =>
          }
        case _ =>
      }
    }
    AviatorBoolean.valueOf(arg1.compare(arg2, env) < 0)
  }
}

@IOpFunction(OperatorType.LE)
class OpLeFunction extends AbstractFunction {
  override def getName: String = OperatorType.LE.getToken

  override def call(env: JavaMap[String, AnyRef], arg1: AviatorObject, arg2: AviatorObject): AviatorObject = {
    if (arg1.getAviatorType == AviatorType.JavaType) {
      FunctionUtils.getJavaObject(arg1, env) match {
        case left: Duration =>
          arg2.getAviatorType match {
            case AviatorType.JavaType =>
              FunctionUtils.getJavaObject(arg2, env) match {
                case right: Duration =>
                  return AviatorRuntimeJavaType.valueOf(left <= right)
                case _ =>
              }
            case _ =>
          }
        case left: Date =>
          arg2.getAviatorType match {
            case AviatorType.JavaType =>
              FunctionUtils.getJavaObject(arg2, env) match {
                case right: Date =>
                  return AviatorRuntimeJavaType.valueOf(left.getTime <= right.getTime)
                case _ =>
              }
            case AviatorType.Long =>
              return AviatorRuntimeJavaType.valueOf(left.getTime <= FunctionUtils.getNumberValue(arg2, env).longValue())
            case _ =>
          }
        case _ =>
      }
    }
    AviatorBoolean.valueOf(arg1.compare(arg2, env) <= 0)
  }
}

@IOpFunction(OperatorType.EQ)
class OpEqFunction extends AbstractFunction {
  override def getName: String = OperatorType.EQ.getToken

  override def call(env: JavaMap[String, AnyRef], arg1: AviatorObject, arg2: AviatorObject): AviatorObject = {
    if (arg1.getAviatorType == AviatorType.JavaType) {
      FunctionUtils.getJavaObject(arg1, env) match {
        case left: Duration =>
          arg2.getAviatorType match {
            case AviatorType.JavaType =>
              FunctionUtils.getJavaObject(arg2, env) match {
                case right: Duration =>
                  return AviatorRuntimeJavaType.valueOf(left == right)
                case _ =>
              }
            case _ =>
          }
        case left: Date =>
          arg2.getAviatorType match {
            case AviatorType.JavaType =>
              FunctionUtils.getJavaObject(arg2, env) match {
                case right: Date =>
                  return AviatorRuntimeJavaType.valueOf(left.getTime == right.getTime)
                case _ =>
              }
            case AviatorType.Long =>
              return AviatorRuntimeJavaType.valueOf(left.getTime == FunctionUtils.getNumberValue(arg2, env).longValue())
            case _ =>
          }
        case _ =>
      }
    }
    AviatorBoolean.valueOf(arg1.compare(arg2, env) == 0)
  }
}

@IOpFunction(OperatorType.NEQ)
class OpNEqFunction extends AbstractFunction {
  override def getName: String = OperatorType.NEQ.getToken

  override def call(env: JavaMap[String, AnyRef], arg1: AviatorObject, arg2: AviatorObject): AviatorObject = {
    if (arg1.getAviatorType == AviatorType.JavaType) {
      FunctionUtils.getJavaObject(arg1, env) match {
        case left: Duration =>
          arg2.getAviatorType match {
            case AviatorType.JavaType =>
              FunctionUtils.getJavaObject(arg2, env) match {
                case right: Duration =>
                  return AviatorRuntimeJavaType.valueOf(left != right)
                case _ =>
              }
            case _ =>
          }
        case left: Date =>
          arg2.getAviatorType match {
            case AviatorType.JavaType =>
              FunctionUtils.getJavaObject(arg2, env) match {
                case right: Date =>
                  return AviatorRuntimeJavaType.valueOf(left.getTime != right.getTime)
                case _ =>
              }
            case AviatorType.Long =>
              return AviatorRuntimeJavaType.valueOf(left.getTime != FunctionUtils.getNumberValue(arg2, env).longValue())
            case _ =>
          }
        case _ =>
      }
    }
    AviatorBoolean.valueOf(arg1.compare(arg2, env) != 0)
  }
}