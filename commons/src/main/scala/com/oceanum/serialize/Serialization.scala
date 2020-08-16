package com.oceanum.serialize

import com.oceanum.common.SystemInit

trait Serialization[W<:WrappedObject] {
  type DefSerializable[T<:AnyRef] = Serializable[T, W]
  def register[T<:AnyRef](serializable: DefSerializable[T]): DefSerializable[T]
  def register[T<:AnyRef](name: String)(implicit mf: Manifest[T]): DefSerializable[T]
  def register[T<:AnyRef](implicit mf: Manifest[T]): DefSerializable[T]
  def register[T<:AnyRef](clazz: Class[T]): DefSerializable[T] = register(Manifest.classType[T](clazz))
  def register[T<:AnyRef](name: String, clazz: Class[T]): DefSerializable[T] = register(name)(Manifest.classType[T](clazz))
  def unRegister(name: String): Unit
  def unRegister[T<:AnyRef](implicit mf: Manifest[T]): Unit
  def unRegister[T<:AnyRef](clazz: Class[T]): Unit = unRegister(Manifest.classType[T](clazz))
  def serialize(obj: AnyRef, pretty: Boolean = false): String
  def deSerialize(str: String): AnyRef
  def deSerializeRaw[T<:AnyRef](str: String)(implicit mf: Manifest[T]): T
}

object Serialization {
  lazy val default: Serialization[_] = SystemInit.serialization
}