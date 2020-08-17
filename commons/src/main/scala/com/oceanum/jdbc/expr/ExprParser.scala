package com.oceanum.jdbc.expr

import com.oceanum.common.StringParser

object ExprParser extends StringParser[JavaMap[String, AnyRef]] {
  override protected def replace(regex: String)(implicit env: JavaMap[String, AnyRef]): String = {
    val regValue = Evaluator.rawExecute(regex, env)
    if (regValue == null)
      throw new NullPointerException("result of：[" + regex + "] is null")
    else
      regValue.toString
  }
}
