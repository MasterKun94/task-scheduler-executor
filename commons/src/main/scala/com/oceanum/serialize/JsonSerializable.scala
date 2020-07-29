package com.oceanum.serialize

import org.json4s.{Extraction, Formats, JValue}

abstract class JsonSerializable[T<:AnyRef](serialization: JsonSerialization) {
  implicit protected val formats: Formats = serialization.formats

  def toJson(t: AnyRef): JValue = Extraction.decompose(t)

  def fromJson(value: JValue): T = value.extract(formats, Manifest.classType(objIdentifier()))

  def objName(): String = objIdentifier().getName

  def objIdentifier(): Class[T]
}

object JsonSerializable {
  def apply[T<:AnyRef](clazz: Class[T])(serialization: JsonSerialization): JsonSerializable[T] = new JsonSerializable[T](serialization) {
    override def objIdentifier(): Class[T] = clazz
  }

  def apply[T<:AnyRef](name: String, clazz: Class[T])(serialization: JsonSerialization): JsonSerializable[T] = new JsonSerializable[T](serialization) {
    override def objName(): String = name
    override def objIdentifier(): Class[T] = clazz
  }
}
