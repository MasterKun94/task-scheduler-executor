package com.oceanum.common

import java.util.Properties

import com.oceanum.expr.Evaluator

import scala.util.matching.Regex

object StringParser {
  val pattern: Regex = """(.*)\$\{(.*)}(.*)""".r  //新建一个正则表达式

  def parseEnv(line: String, prop: Properties): String = {
    if (line == null || line.trim.isEmpty)
      ""
    else
      line match {
        case pattern(pre, reg, str) =>
          val regValue = parseEnv(prop.getProperty(reg, System.getenv(reg)), prop)
          prop.setProperty(reg, regValue)
          if (regValue == null || regValue.trim.isEmpty) {
            throw new RuntimeException("需要在配置文件或环境变量中设置变量：" + reg)
          }
          parseEnv(pre, prop) + regValue + parseEnv(str, prop)
        case str: String => str
        case unknown =>
          println(unknown)
          unknown
      }
  }

  def parseExpr(expr: String)(implicit env: Map[String, Any]): String = {
    if (expr == null || expr.trim.isEmpty)
      ""
    else
      expr match {
        case pattern(pre, reg, str) =>
          val regValue = Evaluator.execute(reg, env)
          val value = if (regValue == null) "" else regValue.toString
          parseExpr(pre) + value + parseExpr(str)
        case str: String => str
        case unknown =>
          println(unknown)
          unknown
      }
  }
}
