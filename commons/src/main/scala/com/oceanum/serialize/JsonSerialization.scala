package com.oceanum.serialize

import org.json4s._
import org.json4s.jackson.{JsonMethods, Serialization}

import scala.collection.mutable

class JsonSerialization(implicit val formats: Formats,
                        protected val serializableMap: mutable.Map[String, JsonSerializable[_<:AnyRef]] = mutable.Map(),
                        protected val names: mutable.Map[Class[_<:AnyRef], String] = mutable.Map()) {
  private val autoType = true

  def withJsonSerializer[T](serializer: Serializer[T]): JsonSerialization = {
    new JsonSerialization()(formats = formats + serializer, serializableMap, names)
  }

  def register[T<:AnyRef](serializable: JsonSerializable[T]): JsonSerializable[T] = {
    serializableMap += (serializable.objName -> serializable)
    names += (serializable.objIdentifier() -> serializable.objName())
    serializable
  }

  def register[T<:AnyRef](name: String, clazz: Class[T]): JsonSerializable[T] = {
    register(JsonSerializable(name, clazz)(this))
  }

  def register[T<:AnyRef](clazz: Class[T]): JsonSerializable[T] = {
    register(JsonSerializable(clazz)(this))
  }

  def unRegister[T<:AnyRef](clazz: Class[T]): Unit = {
    names.remove(clazz).foreach {
      serializableMap.remove
    }
  }

  def unRegister(name: String): Unit = {
    serializableMap.remove(name)
    names.find(_._2.equals(name)).map(_._1).foreach(names.remove)
  }

  def serialize(obj: AnyRef, pretty: Boolean = false): String = {
    val name = names.getOrElse(obj.getClass, obj.getClass.getName)
    val serializer = serializableMap.get(name) match {
      case Some(serializer) =>
        serializer
      case None =>
        if (autoType) register(obj.getClass)
        else throw new IllegalArgumentException("can not serialize obj: " + obj.getClass)
    }
    val jObj = serializer.toJson(obj) merge JObject("@type" -> JString(serializer.objName()))
    if (pretty) {
      Serialization.writePretty(jObj)
    } else {
      Serialization.write(jObj)
    }
  }

  def deSerialize(str: String): AnyRef = {
    val jValue = JsonMethods.parse(str)
    val name = (jValue \ "@type").extract[String]
    val serializer = serializableMap.get(name) match {
      case Some(serializer) =>
        serializer
      case None =>
        if (autoType) register(Class.forName(name).asInstanceOf[Class[_<:AnyRef]])
        else throw new IllegalArgumentException("can not serialize obj by type: " + name + ", json: " + str)
    }
    serializer.fromJson(jValue)
  }

  def deSerialize[T](str: String, clazz: Class[T]): T = {
    Serialization.read(str)(formats, Manifest.classType(clazz))
  }
}