package com.oceanum.expr

import com.googlecode.aviator.AviatorEvaluator

/**
 * @author chenmingkun
 * @date 2020/7/16
 */
object ExprParser {
  def execute(expr: String, env: Map[String, Any]): Any = {
    val javaEnv = new java.util.HashMap[String, AnyRef](env.size)
    for (elem <- env) {
      javaEnv.put(elem._1, elem._2.asInstanceOf[AnyRef])
    }
    AviatorEvaluator.execute(expr, javaEnv)
  }

  def main(args: Array[String]): Unit = {
    val expr = "1 + 2 + three"
    val env = Map("three" -> 3)
    println(execute(expr, env))
  }
}
