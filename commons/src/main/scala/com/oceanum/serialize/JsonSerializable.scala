package com.oceanum.serialize

import org.json4s.{Extraction, Formats, JObject, JString, JValue}

abstract class JsonSerializable[T<:AnyRef](serialization: JsonSerialization)(implicit mf: Manifest[T])
  extends Serializable[T, JsonObject] {
  implicit protected val formats: Formats = serialization.formats

  def runtimeClass: Class[_] = mf.runtimeClass

  def toJson(t: AnyRef): JsonObject = {
    new JsonObject(Extraction.decompose(t) merge JObject("@type" -> JString(objName)))
  }

  def fromJson(value: JsonObject): T = {
    value.deserializedObject(mf)
  }

  def objName: String = mf.runtimeClass.getName
}

object JsonSerializable {
  def apply[T<:AnyRef](serialization: JsonSerialization)(implicit mf: Manifest[T]): JsonSerializable[T] = new JsonSerializable[T](serialization) {
  }

  def apply[T<:AnyRef](name: String, serialization: JsonSerialization)(implicit mf: Manifest[T]): JsonSerializable[T] = new JsonSerializable[T](serialization) {
    override def objName: String = name
  }
}
