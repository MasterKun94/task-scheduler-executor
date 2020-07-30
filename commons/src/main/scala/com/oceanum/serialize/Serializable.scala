package com.oceanum.serialize

trait Serializable[T<:AnyRef, W<:WrappedObject] {
  def runtimeClass: Class[_]

  def toJson(t: AnyRef): W

  def fromJson(value: W): T

  def objName: String
}
