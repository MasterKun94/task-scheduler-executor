package com.oceanum.expr

import java.util.concurrent.TimeUnit

import com.googlecode.aviator.runtime.`type`.{AviatorObject, AviatorRuntimeJavaType, AviatorType}
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
    val duration = num.getAviatorType match {
      case AviatorType.Long | AviatorType.BigInt =>
        Duration(FunctionUtils.getNumberValue(num, env).longValue(), TimeUnit.MILLISECONDS)
      case AviatorType.Double | AviatorType.Decimal =>
        Duration(FunctionUtils.getNumberValue(num, env).doubleValue(), TimeUnit.MILLISECONDS)
      case AviatorType.String =>
        Duration(FunctionUtils.getStringValue(num, env).toDouble, TimeUnit.MILLISECONDS)
    }
    AviatorRuntimeJavaType.valueOf(duration)
  }
}

class DurationMilliFunction extends DurationMillisFunction {
  override def getName: String = "duration.milli"
}

class DurationSecondsFunction extends AbstractFunction {
  override def getName: String = "duration.seconds"

  override def call(env: JavaMap[String, AnyRef], num: AviatorObject): AviatorObject = {
    val duration = num.getAviatorType match {
      case AviatorType.Long | AviatorType.BigInt =>
        Duration(FunctionUtils.getNumberValue(num, env).longValue(), TimeUnit.SECONDS)
      case AviatorType.Double | AviatorType.Decimal =>
        Duration(FunctionUtils.getNumberValue(num, env).doubleValue(), TimeUnit.SECONDS)
      case AviatorType.String =>
        Duration(FunctionUtils.getStringValue(num, env).toDouble, TimeUnit.SECONDS)
    }
    AviatorRuntimeJavaType.valueOf(duration)
  }
}

class DurationSecondFunction extends DurationSecondsFunction {
  override def getName: String = "duration.second"
}

class DurationMinutesFunction extends AbstractFunction {
  override def getName: String = "duration.minutes"

  override def call(env: JavaMap[String, AnyRef], num: AviatorObject): AviatorObject = {
    val duration = num.getAviatorType match {
      case AviatorType.Long | AviatorType.BigInt =>
        Duration(FunctionUtils.getNumberValue(num, env).longValue(), TimeUnit.MINUTES)
      case AviatorType.Double | AviatorType.Decimal =>
        Duration(FunctionUtils.getNumberValue(num, env).doubleValue(), TimeUnit.MINUTES)
      case AviatorType.String =>
        Duration(FunctionUtils.getStringValue(num, env).toDouble, TimeUnit.MINUTES)
    }
    AviatorRuntimeJavaType.valueOf(duration)
  }
}

class DurationMinuteFunction extends DurationMinutesFunction {
  override def getName: String = "duration.minute"
}

class DurationHoursFunction extends AbstractFunction {
  override def getName: String = "duration.hours"

  override def call(env: JavaMap[String, AnyRef], num: AviatorObject): AviatorObject = {
    val duration = num.getAviatorType match {
      case AviatorType.Long | AviatorType.BigInt =>
        Duration(FunctionUtils.getNumberValue(num, env).longValue(), TimeUnit.HOURS)
      case AviatorType.Double | AviatorType.Decimal =>
        Duration(FunctionUtils.getNumberValue(num, env).doubleValue(), TimeUnit.HOURS)
      case AviatorType.String =>
        Duration(FunctionUtils.getStringValue(num, env).toDouble, TimeUnit.HOURS)
    }
    AviatorRuntimeJavaType.valueOf(duration)
  }
}

class DurationHourFunction extends DurationHoursFunction {
  override def getName: String = "duration.hour"
}

class DurationDaysFunction extends AbstractFunction {
  override def getName: String = "duration.days"

  override def call(env: JavaMap[String, AnyRef], num: AviatorObject): AviatorObject = {
    val duration = num.getAviatorType match {
      case AviatorType.Long | AviatorType.BigInt =>
        Duration(FunctionUtils.getNumberValue(num, env).longValue(), TimeUnit.DAYS)
      case AviatorType.Double | AviatorType.Decimal =>
        Duration(FunctionUtils.getNumberValue(num, env).doubleValue(), TimeUnit.DAYS)
      case AviatorType.String =>
        Duration(FunctionUtils.getStringValue(num, env).toDouble, TimeUnit.DAYS)
    }
    AviatorRuntimeJavaType.valueOf(duration)
  }
}

class DurationDayFunction extends DurationDaysFunction {
  override def getName: String = "duration.day"
}
