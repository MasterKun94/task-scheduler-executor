package com.oceanum.expr

import com.oceanum.common.StringParser

object ValidateParser extends StringParser[JavaMap[String, AnyRef]] {
  override protected def replace(regex: String)(implicit env: JavaMap[String, AnyRef]): String = {
    Evaluator.compile(regex, cache = false)
    ""
  }

  override def parse(line: String)(implicit env: JavaMap[String, AnyRef] = null): String = super.parse(line)
}
