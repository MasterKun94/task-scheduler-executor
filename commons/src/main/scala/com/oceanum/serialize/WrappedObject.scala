package com.oceanum.serialize

trait WrappedObject {
  def objType: String

  def serializedString[T<:AnyRef](pretty: Boolean = false): String

  def deserializedObject[T<:AnyRef](mf: Manifest[T]): T
}


