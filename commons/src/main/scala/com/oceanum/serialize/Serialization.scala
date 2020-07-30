package com.oceanum.serialize

import com.oceanum.common.{FallbackStrategy, GraphStatus, ReRunStrategy, TaskStatus}
import org.json4s.{DefaultFormats, Formats}
import org.json4s.ext.EnumNameSerializer

trait Serialization[W<:WrappedObject] {
  type DefSerializable[T<:AnyRef] = Serializable[T, W]
  def register[T<:AnyRef](serializable: DefSerializable[T]): DefSerializable[T]
  def register[T<:AnyRef](name: String)(implicit mf: Manifest[T]): DefSerializable[T]
  def register[T<:AnyRef](implicit mf: Manifest[T]): DefSerializable[T]
  def register[T<:AnyRef](clazz: Class[T]): DefSerializable[T] = register(Manifest.classType[T](clazz))
  def unRegister(name: String): Unit
  def unRegister[T<:AnyRef](implicit mf: Manifest[T]): Unit
  def unRegister[T<:AnyRef](clazz: Class[T]): Unit = unRegister(Manifest.classType[T](clazz))
  def serialize[T<:AnyRef](obj: T, pretty: Boolean = false): String
  def deSerialize(str: String): AnyRef
  def deSerializeRaw[T<:AnyRef](str: String)(implicit mf: Manifest[T]): T
}

object Serialization {
  implicit private val formats: Formats = DefaultFormats +
  new EnumNameSerializer(FallbackStrategy) +
    new EnumNameSerializer(ReRunStrategy) +
    new EnumNameSerializer(GraphStatus) +
    new EnumNameSerializer(TaskStatus) +
    new ThrowableSerializer() +
    new StackTraceElementSerializer()

  lazy val default: Serialization[_] = new JsonSerialization()
}
