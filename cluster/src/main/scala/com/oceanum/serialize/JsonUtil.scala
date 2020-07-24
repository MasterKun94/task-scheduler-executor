package com.oceanum.serialize

import com.oceanum.client.{Task, TaskProp}
import com.oceanum.common.{ExprContext, RichGraphMeta}
import com.oceanum.exec.State
import com.oceanum.graph.{FallbackStrategy, GraphStatus, ReRunStrategy}
import org.json4s.ext.EnumNameSerializer
import org.json4s.jackson.{JsonMethods, Serialization}
import org.json4s.{DefaultFormats, Extraction, Formats, JObject, JString, JValue}

import scala.collection.mutable

object JsonUtil {
  implicit private val formats: Formats = DefaultFormats +
    new EnumNameSerializer(FallbackStrategy) +
    new EnumNameSerializer(ReRunStrategy) +
    new EnumNameSerializer(GraphStatus) +
    new EnumNameSerializer(State)
  private val taskPropRegistry: mutable.Map[String, Manifest[_]] = mutable.Map()

  def registerTaskProp[T<:TaskProp](taskType: String, clazz: Class[T]): Unit = {
    taskPropRegistry += (taskType -> Manifest.classType(clazz))
  }

  def json2task(json: String): Task = {
    val jValue = JsonMethods.parse(json)
    val taskType: String = (jValue \ "type").extract[String]

    val prop: TaskProp = (jValue \ "prop").extract(formats, taskPropRegistry(taskType)).asInstanceOf[TaskProp]
    jValue
      .removeField(_._1.equals("prop"))
      .extract[Task]
      .copy(prop = prop)
  }

  def task2json(task: Task, pretty: Boolean = true): String = {
    val value: JValue = Extraction.decompose(task) merge JObject("type" -> JString(task.prop.taskType))
    toString(value, pretty)
  }

  def json2context(json: String): ExprContext = {
    JsonMethods.parse(json)
      .extractOpt[ExprContext]
      .get
  }

  def json2graphMeta(json: String): RichGraphMeta = {
    JsonMethods.parse(json).extract[RichGraphMeta]
  }

  def toString[A<:AnyRef](jValue: A, pretty: Boolean = true): String = {
    if (pretty) Serialization.writePretty(jValue)
    else Serialization.write(jValue)
  }

  def map2obj[T](map: Map[_, _], clazz: Class[T]): T = {
    Extraction.decompose(map).extract[T](mf = Manifest.classType(clazz), formats = formats)
  }
}
