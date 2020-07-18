package aviator

import java.util

import com.googlecode.aviator.AviatorEvaluator
import com.oceanum.common.StringParser
import com.oceanum.expr.Evaluator

/**
 * @author chenmingkun
 * @date 2020/7/16
 */
object Test extends App {
  val expr = "file_name"
  val env = Map("file_name" -> "python")
  println(Evaluator.execute(expr, env))
}
