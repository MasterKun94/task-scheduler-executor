package com.oceanum.expr

import com.googlecode.aviator.runtime.`type`.AviatorObject
import com.googlecode.aviator.runtime.function.AbstractFunction
import com.oceanum.annotation.IFunction

/**
 * @author chenmingkun
 * @date 2020/7/26
 */
@IFunction
class OptionFunction extends AbstractFunction {
  override def getName: String = "option"

  override def call(env: JavaMap[String, AnyRef], nullable: AviatorObject, orElse: AviatorObject): AviatorObject = {
    if (nullable.isNull(env)) orElse
    else nullable
  }
}

@IFunction
class OptionalFunction extends AbstractFunction {
  override def getName: String = "optional"
}
