package com.oceanum.expr

import com.googlecode.aviator.runtime.`type`.AviatorObject
import com.googlecode.aviator.runtime.function.AbstractFunction

/**
 * @author chenmingkun
 * @date 2020/7/26
 */
class OptionFunction extends AbstractFunction {
  override def getName: String = "option"

  override def call(env: JavaMap[String, AnyRef], nullable: AviatorObject, orElse: AviatorObject): AviatorObject = {
    if (nullable.isNull(env)) orElse
    else nullable
  }
}

class OptionalFunction extends AbstractFunction {
  override def getName: String = "optional"
}
