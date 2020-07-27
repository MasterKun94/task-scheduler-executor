import java.util

import com.oceanum.common.RichTaskMeta
import com.oceanum.serialize.{JsonSerialization, ThrowableSerializer}
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
  def main(args: Array[String]): Unit = {
    val e = new Exception("this is a test")
    val i = new IllegalArgumentException("test test", e)
    JsonSerialization.init()
    i.printStackTrace()
    val taskMeta = new RichTaskMeta().copy(error = i)
    JsonSerialization.register(classOf[IllegalArgumentException])
    val str = JsonSerialization.serialize(taskMeta)
    println(str)
    JsonSerialization.deSerialize(str).asInstanceOf[RichTaskMeta].error.printStackTrace()
  }
}
