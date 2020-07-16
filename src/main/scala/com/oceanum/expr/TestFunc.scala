package com.oceanum.expr

import com.googlecode.aviator.runtime.`type`.AviatorObject
import com.googlecode.aviator.runtime.function.AbstractFunction

/**
 * @author chenmingkun
 * @date 2020/7/17
 */
class TestFunc extends AbstractFunction {
  override def call(env: JavaMap[String, AnyRef], arg1: AviatorObject, arg2: AviatorObject): AviatorObject = {
    super.call(env, arg1, arg2)
  }

  override def getName: String = "test"
}
