import com.oceanum.Test.Test
import com.oceanum.client.Task
import com.oceanum.common.{ExprContext, RichGraphMeta}
import com.oceanum.serialize.JsonUtil
import org.json4s.jackson.{JsonMethods, Serialization}
import org.json4s._

object JsonTest {
  implicit val formats: Formats = DefaultFormats

  def fromJson(json: String): Task = {
    JsonMethods.parse(json).extract[Task]
  }

  def toJson(task: Task, pretty: Boolean = false): String = {
    if (pretty) {
      Serialization.writePretty(task)
    } else {
      Serialization.write(task)
    }
  }

  def main(args: Array[String]): Unit = {

    val ctx = new ExprContext(Map.empty, graphMeta = new RichGraphMeta())
    val str = JsonUtil.toString(ctx)
    println(str)
    val c: ExprContext = JsonUtil.json2context(str)
    println(c.exprEnv)
    println(c.graphMeta)
    println(c.graphMeta)
  }
}
