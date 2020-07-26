import java.util

import com.oceanum.serialize.ThrowableSerializer
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
    val mapper = new ObjectMapper()
    val e = new Exception("this is a test")

    implicit val formats = DefaultFormats
    val str = mapper.writeValueAsString(mapper)
    val jObject = Serialization.read[JObject](str)

    val map = jObject.values
  }
}
