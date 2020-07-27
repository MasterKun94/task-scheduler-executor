package com.oceanum.serialize

import org.json4s.Formats
import org.json4s.JValue

abstract class JsonSerializable[T<:AnyRef] {
  implicit protected val formats: Formats = JsonSerialization.formats

  def toJson(t: AnyRef): JValue

  def fromJson(value: JValue): T

  def objName(): String = objIdentifier().getName

  def objIdentifier(): Class[T]
}
