package com.oceanum.serialize

import org.json4s.JsonAST.JValue

trait JsonSerializable {
  def toJson(t: AnyRef): JValue

  def fromJson(str: JValue): AnyRef

  def objName(): String
}
