package com.oceanum.expr

import java.util.Date

import com.googlecode.aviator.{AviatorEvaluator, Expression}
import com.googlecode.aviator.lexer.token.OperatorType
import com.googlecode.aviator.runtime.`type`.AviatorFunction
import com.oceanum.common.{Environment, GraphContext}

/**
 * @author chenmingkun
 * @date 2020/7/16
 */
object Evaluator {
  def addFunction(function: AviatorFunction): Unit = AviatorEvaluator.addFunction(function)

  def addOpFunction(operatorType: OperatorType, function: AviatorFunction): Unit = AviatorEvaluator.addOpFunction(operatorType, function)

  def execute(expr: String, env: GraphContext): Any = rawExecute(expr, env.javaExprEnv)

  def rawExecute(expr: String, env: JavaMap[String, AnyRef]): Any = {
    if (env.isEmpty) AviatorEvaluator.execute(expr)
    else AviatorEvaluator.execute(expr, env, Environment.AVIATOR_CACHE_ENABLED)
  }

  def compile(expr: String, cache: Boolean = Environment.AVIATOR_CACHE_ENABLED): Expression = {
    AviatorEvaluator.compile(expr, cache)
  }
}