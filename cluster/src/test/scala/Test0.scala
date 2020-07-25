import com.oceanum.Test.Test
import com.oceanum.common.GraphContext
import com.oceanum.graph.GraphDefine
import com.oceanum.graph.Operators.{Converge, Decision, DecisionOut, End, Fork, Join, Start, TaskOperator}
import com.oceanum.serialize.{JsonSerialization, OperatorSerializer}

import scala.util.matching.Regex

/**
 * @author chenmingkun
 * @date 2020/7/25
 */
object Test0 {
  def main(args: Array[String]): Unit = {
    val pattern: Regex = """(.*)\$\{(.*)}(.*)""".r

  }
}
