package com.oceanum.expr

import java.text.SimpleDateFormat
import java.util.Date

import com.googlecode.aviator.runtime.`type`.{AviatorObject, AviatorRuntimeJavaType, AviatorType}
import com.googlecode.aviator.runtime.function.{AbstractFunction, FunctionUtils}
import com.oceanum.common.DateUtil

import scala.concurrent.duration.Duration

/**
 * @author chenmingkun
 * @date 2020/7/17
 */
class DateFormatFunction extends AbstractFunction {
  override def getName: String = "date.format"

  override def call(env: JavaMap[String, AnyRef], format: AviatorObject): AviatorObject = {
    val str = FunctionUtils.getStringValue(format, env)
    AviatorRuntimeJavaType.valueOf(DateUtil.format(str).format(new Date()))
  }

  override def call(env: JavaMap[String, AnyRef], format: AviatorObject, date: AviatorObject): AviatorObject = {
    val date0: Date = date.getAviatorType match {
      case AviatorType.JavaType => FunctionUtils.getJavaObject(date, env) match {
        case d: Date => d
        case d: Duration => new Date(System.currentTimeMillis() + d.toMillis)
        case _ => throw new IllegalArgumentException("illegal argument type: " + date.getAviatorType)
      }
      case AviatorType.String =>
        val duration = Duration(FunctionUtils.getStringValue(date, env))
        new Date(System.currentTimeMillis() + duration.toMillis)
      case AviatorType.Long =>
        new Date(FunctionUtils.getNumberValue(date, env).longValue())
      case _ =>
        throw new IllegalArgumentException("illegal argument type: " + date.getAviatorType)
    }
    val str = FunctionUtils.getStringValue(format, env)
    AviatorRuntimeJavaType.valueOf(DateUtil.format(str).format(date0))
  }

  override def call(env: JavaMap[String, AnyRef], format: AviatorObject, date: AviatorObject, shift: AviatorObject): AviatorObject = {
    val duration = shift.getAviatorType match {
      case AviatorType.JavaType =>
        FunctionUtils.getJavaObject(shift, env).asInstanceOf[Duration]
      case AviatorType.String =>
        Duration(FunctionUtils.getStringValue(shift, env))
      case _ =>
        throw new IllegalArgumentException("illegal argument type: " + shift.getAviatorType)
    }
    val date0: Date = date.getAviatorType match {
      case AviatorType.JavaType => FunctionUtils.getJavaObject(date, env).asInstanceOf[Date]
      case AviatorType.Long => new Date(FunctionUtils.getNumberValue(date, env).longValue())
      case _ => throw new IllegalArgumentException("illegal argument type: " + date.getAviatorType)
    }
    val date1: Date = new Date(date0.getTime + duration.toMillis)
    val str = FunctionUtils.getStringValue(format, env)
    AviatorRuntimeJavaType.valueOf(DateUtil.format(str).format(date1))
  }
}

class DateParseFunction extends AbstractFunction {
  override def getName: String = "date.parse"

  override def call(env: JavaMap[String, AnyRef], format: AviatorObject, dateStr: AviatorObject): AviatorObject = {
    val dateStr0 = FunctionUtils.getStringValue(dateStr, env)
    val format0 = FunctionUtils.getStringValue(format, env)
    AviatorRuntimeJavaType.valueOf(DateUtil.format(format0).parse(dateStr0))
  }
}

class DateShiftFunction extends AbstractFunction {
  override def getName: String = "date.shift"

  override def call(env: JavaMap[String, AnyRef], duration: AviatorObject): AviatorObject = {
    val duration0: Duration = FunctionUtils.getJavaObject(duration, env).asInstanceOf[Duration]
    AviatorRuntimeJavaType.valueOf(new Date(System.currentTimeMillis() + duration0.toMillis))
  }
}

class DateNowFunction extends AbstractFunction {
  override def getName: String = "date.now"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = {
    AviatorRuntimeJavaType.valueOf(new Date())
  }
}