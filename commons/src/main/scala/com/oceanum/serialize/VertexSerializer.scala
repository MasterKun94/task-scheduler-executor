package com.oceanum.serialize

import com.oceanum.api.entities.{ConvergeVertex, DecisionVertex, EndVertex, ForkVertex, JoinVertex, StartVertex, TaskVertex, Vertex}
import org.json4s.{CustomSerializer, DefaultFormats, Extraction, Formats, JObject, JString}

/**
 * @author chenmingkun
 * @date 2020/7/25
 */
class VertexSerializer(operatorClasses: Set[Class[_<:Vertex]]) extends CustomSerializer[Vertex](_ => {
  implicit val formats: Formats = DefaultFormats + TaskSerializer.default()
  val map: Map[String, (Manifest[Vertex], JObject)] = operatorClasses
    .map(clazz => clazz.getSimpleName -> (Manifest.classType[Vertex](clazz) -> JObject("operatorType" -> JString(clazz.getSimpleName))))
    .toMap
  (
    {
      case value: JObject =>
        val taskType = (value \ "operatorType").extract[String]
        value.extract(formats, map(taskType)._1)
    },
    {
      case operator: Vertex =>
        Extraction.decompose(operator) merge JObject("operatorType" -> JString(operator.getClass.getSimpleName))
    }
  )
}
)

object VertexSerializer {
  def apply(operatorClasses: Class[_<:Vertex]*): VertexSerializer = {
    new VertexSerializer(operatorClasses.toSet)
  }

  def default(): VertexSerializer = {
    VertexSerializer (
      classOf[TaskVertex],
      classOf[ForkVertex],
      classOf[JoinVertex],
      classOf[DecisionVertex],
      classOf[ConvergeVertex],
      classOf[StartVertex],
      classOf[EndVertex]
    )
  }
}