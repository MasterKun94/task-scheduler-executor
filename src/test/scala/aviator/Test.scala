package aviator

import java.util

import com.googlecode.aviator.AviatorEvaluator
import com.oceanum.common.StringParser
import com.oceanum.expr.ExprParser

/**
 * @author chenmingkun
 * @date 2020/7/16
 */
object Test extends App {
  val expr = "file_name"
  val env = Map("file_name" -> "python")
  println(ExprParser.execute(expr, env))
}
