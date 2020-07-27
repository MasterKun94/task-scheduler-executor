package com.oceanum.serialize

import com.oceanum.graph.Operators.{Converge, Decision, DecisionOut, End, Fork, Join, Operator, Start, TaskOperator}
import com.oceanum.graph.StreamFlows.StreamFlow
import org.json4s.{CustomSerializer, DefaultFormats, Extraction, Formats, JObject, JString}

/**
 * @author chenmingkun
 * @date 2020/7/25
 */
class OperatorSerializer(operatorClasses: Set[Class[_<:Operator[_<:StreamFlow]]]) extends CustomSerializer[Operator[_<:StreamFlow]](_ => {
  implicit val formats: Formats = DefaultFormats + TaskSerializer.default()
  val map: Map[String, (Manifest[_<:Operator[_<:StreamFlow]], JObject)] = operatorClasses
    .map(clazz => clazz.getSimpleName -> (Manifest.classType(clazz) -> JObject("operatorType" -> JString(clazz.getSimpleName))))
    .toMap
  (
    {
      case value: JObject =>
        val taskType = (value \ "operatorType").extract[String]
        value.extract(formats, map(taskType)._1)
    },
    {
      case operator: Operator[_] =>
        Extraction.decompose(operator) merge JObject("operatorType" -> JString(operator.getClass.getSimpleName))
    }
  )
}
)

object OperatorSerializer {
  def apply(operatorClasses: Class[_ <: Operator[_ <: StreamFlow]]*): OperatorSerializer = {
    new OperatorSerializer(operatorClasses.toSet)
  }

  def default(): OperatorSerializer = {
    OperatorSerializer (
      classOf[TaskOperator],
      classOf[Fork],
      classOf[Join],
      classOf[Decision],
      classOf[DecisionOut],
      classOf[Converge],
      classOf[Start],
      classOf[End]
    )
  }
}