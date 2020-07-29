import java.util
import java.util.Date

import com.oceanum.Test.Test
import com.oceanum.common.{Environment, RichGraphMeta, RichTaskMeta}
import com.oceanum.serialize.{DefaultJsonSerialization, ThrowableSerializer}
import org.codehaus.jackson.{JsonNode, JsonParser}
import org.codehaus.jackson.map.ObjectMapper
import org.codehaus.jackson.node.{JsonNodeFactory, ObjectNode}
import org.json4s.JsonAST.JObject
import org.json4s.{DefaultFormats, Extraction}
import org.json4s.ext.JavaTypesSerializers
import org.json4s.jackson.{JsonMethods, Serialization}

/**
 * @author chenmingkun
 * @date 2020/7/25
 */
object Test0 {
  def print(s: Int): Unit = {
    val e = new Exception("this is a test")
    val i = new IllegalArgumentException("test test", e)
    val taskMeta = new RichTaskMeta().withTask(Test.task().addGraphMeta(new RichGraphMeta().copy(startTime = new Date()))).copy(error = i)
    val str = DefaultJsonSerialization.serialize(taskMeta)
    val meta = DefaultJsonSerialization.deSerialize(str).asInstanceOf[RichTaskMeta]
//    println(str)
    println(s + " -> " + meta)
  }

  def main(args: Array[String]): Unit = {
    for (i <- 1 to 20) {
      print(i)
    }
  }
}
