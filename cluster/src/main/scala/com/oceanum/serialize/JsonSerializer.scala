package com.oceanum.serialize

import com.oceanum.exec.State
import com.oceanum.graph.{FallbackStrategy, GraphStatus, ReRunStrategy}
import org.json4s.ext.EnumNameSerializer
import org.json4s.jackson.{JsonMethods, Serialization}
import org.json4s.{DefaultFormats, Extraction, Formats, JObject, JString, JValue}

import scala.collection.mutable

class JsonSerializer {
  private val serializers: mutable.Map[String, JsonSerializable] = mutable.Map()
  private val names: mutable.Map[Class[_<:AnyRef], String] = mutable.Map()
  implicit private val formats: Formats = DefaultFormats +
    new EnumNameSerializer(FallbackStrategy) +
    new EnumNameSerializer(ReRunStrategy) +
    new EnumNameSerializer(GraphStatus) +
    new EnumNameSerializer(State)

  def register(serializable: JsonSerializable): Unit = serializers += (serializable.objName -> serializable)

  def register(name: String, clazz: Class[_<:AnyRef]): Unit = {
    val serializable = new JsonSerializable {
      override def toJson(obj: AnyRef): JValue = Extraction.decompose(obj)

      override def fromJson(jValue: JValue): AnyRef = jValue.extract(formats, Manifest.classType(clazz))

      override def objName(): String = name
    }
    register(serializable)
  }

  def register[T<:AnyRef](clazz: Class[T]): Unit = register(clazz.getName, clazz)

  def unRegister(clazz: Class[_<:AnyRef]): Unit = {
    names.remove(clazz).foreach {
      serializers.remove
    }
  }

  def unRegister(name: String): Unit = {
    serializers.remove(name)
    names.find(_._2.equals(name)).map(_._1).foreach(names.remove)
  }

  def serialize(obj: AnyRef, pretty: Boolean = false): String = {
    val name = names(obj.getClass)
    val serializer = serializers(name)
    val jObj = JObject(
      "@type" -> JString(serializer.objName()),
      "value" -> serializer.toJson(obj)
    )
    Serialization.write(jObj)
  }

  def deSerialize(str: String): AnyRef = {
    val jValue = JsonMethods.parse(str)
    val name = (jValue \ "@type").extract[String]
    val serializer = serializers(name)
    serializer.fromJson(jValue \ "value")
  }
}
