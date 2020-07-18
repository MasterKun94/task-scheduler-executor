package com.oceanum.expr

import java.util.concurrent.TimeUnit

import com.googlecode.aviator.runtime.`type`.{AviatorObject, AviatorRuntimeJavaType}
import com.googlecode.aviator.runtime.function.{AbstractFunction, FunctionUtils}

import scala.concurrent.duration.Duration

/**
 * @author chenmingkun
 * @date 2020/7/17
 */
class DurationFunction extends AbstractFunction {
  override def getName: String = "duration"

  override def call(env: JavaMap[String, AnyRef], str: AviatorObject): AviatorObject = {
    val d = FunctionUtils.getStringValue(str, env)
    AviatorRuntimeJavaType.valueOf(Duration(d))
  }
}

class DurationMillisFunction extends AbstractFunction {
  override def getName: String = "duration.millis"

  override def call(env: JavaMap[String, AnyRef], num: AviatorObject): AviatorObject = {
    val number = FunctionUtils.getNumberValue(num, env)
    AviatorRuntimeJavaType.valueOf(Duration(number.longValue(), TimeUnit.MILLISECONDS))
  }
}


class DurationSecondsFunction extends AbstractFunction {
  override def getName: String = "duration.seconds"

  override def call(env: JavaMap[String, AnyRef], num: AviatorObject): AviatorObject = {
    val number = FunctionUtils.getNumberValue(num, env)
    AviatorRuntimeJavaType.valueOf(Duration(number.longValue(), TimeUnit.SECONDS))
  }
}


class DurationMinutesFunction extends AbstractFunction {
  override def getName: String = "duration.minutes"

  override def call(env: JavaMap[String, AnyRef], num: AviatorObject): AviatorObject = {
    val number = FunctionUtils.getNumberValue(num, env)
    AviatorRuntimeJavaType.valueOf(Duration(number.longValue(), TimeUnit.MINUTES))
  }
}

class DurationHoursFunction extends AbstractFunction {
  override def getName: String = "duration.hours"

  override def call(env: JavaMap[String, AnyRef], num: AviatorObject): AviatorObject = {
    val number = FunctionUtils.getNumberValue(num, env)
    AviatorRuntimeJavaType.valueOf(Duration(number.longValue(), TimeUnit.HOURS))
  }
}

class DurationDaysFunction extends AbstractFunction {
  override def getName: String = "duration.days"

  override def call(env: JavaMap[String, AnyRef], num: AviatorObject): AviatorObject = {
    val number = FunctionUtils.getNumberValue(num, env)
    AviatorRuntimeJavaType.valueOf(Duration(number.longValue(), TimeUnit.DAYS))
  }
}