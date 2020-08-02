package com.oceanum.serialize

import com.oceanum.client._
import org.json4s.{CustomSerializer, DefaultFormats, Extraction, Formats, JObject, JString}

/**
 * @author chenmingkun
 * @date 2020/7/25
 */
class TaskSerializer(propClasses: Map[String, Class[_<:TaskProp]]) extends CustomSerializer[Task](_ => {
  implicit val formats: Formats = DefaultFormats
  val map: Map[String, (Manifest[_<:TaskProp], JObject)] = propClasses.map(kv => {
    kv._1 -> (Manifest.classType(kv._2) -> JObject("taskType" -> JString(kv._1)))
  })
  (
    {
      case value: JObject =>
        val taskType = (value \ "taskType").extract[String]
        val taskProp: TaskProp = (value \ "prop").extract(formats, map(taskType)._1)
        value
          .removeField(_._1.equals("prop"))
          .extract[Task]
          .copy(prop = taskProp)
    },
    {
      case task: Task => Extraction.decompose(task) merge map(task.prop.taskType)._2
    }
  )
}
)

object TaskSerializer {
  def apply(propClasses: (String, Class[_ <: TaskProp])*): TaskSerializer = new TaskSerializer(Map(propClasses:_*))

  def default(): TaskSerializer = {
    TaskSerializer (
      "SHELL" -> classOf[ShellTaskProp],
      "SHELL_SCRIPT" -> classOf[ShellScriptTaskProp],
      "JAVA" -> classOf[JavaTaskProp],
      "SCALA" -> classOf[ScalaTaskProp],
      "PYTHON" -> classOf[PythonTaskProp],
      "USER_ADD" -> classOf[UserAdd]
    )
  }

  def main(args: Array[String]): Unit = {
    println(classOf[PythonTaskProp].getSimpleName)
    println(classOf[PythonTaskProp].getTypeName)
    println(classOf[Task].getComponentType)
  }
}