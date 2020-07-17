package com.oceanum.expr

import com.googlecode.aviator.AviatorEvaluator
import com.googlecode.aviator.runtime.`type`.AviatorFunction
import com.oceanum.common.StringParser

/**
 * @author chenmingkun
 * @date 2020/7/16
 */
object ExprParser {
  def addFunction(function: AviatorFunction): Unit = {
    AviatorEvaluator.addFunction(function)
  }

  def execute(expr: String, env: Map[String, Any]): Any = {
    val javaEnv = new JavaHashMap[String, AnyRef](env.size)
    for (elem <- env) {
      javaEnv.put(elem._1, elem._2.asInstanceOf[AnyRef])
    }
    AviatorEvaluator.execute(expr, javaEnv)
  }

  def main(args: Array[String]): Unit = {
    val expr = "/test/hello/${1+2+3}"
    println(StringParser.parseExpr(expr)(Map.empty))
  }
}
