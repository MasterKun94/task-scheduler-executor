package aviator

import com.googlecode.aviator.AviatorEvaluator

/**
 * @author chenmingkun
 * @date 2020/7/16
 */
object Test extends App {
  val expr = "1 + 2 + 3"
  println(AviatorEvaluator.execute(expr))
}
