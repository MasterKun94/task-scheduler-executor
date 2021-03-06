package com.oceanum.serialize

import com.oceanum.common._
import org.json4s._
import org.json4s.jackson.{JsonMethods, Serialization => JackSerial}

import scala.collection.concurrent.TrieMap

class JsonSerialization(protected val serializableMap: TrieMap[String, Serializable[_<:AnyRef, JsonObject]] = TrieMap(),
                        protected val names: TrieMap[Class[_], String] = TrieMap())(implicit val formats: Formats)
  extends Serialization[JsonObject] {
  private val autoType = true

  def withJsonSerializer[T](serializer: Serializer[T]): JsonSerialization = {
    new JsonSerialization(serializableMap, names)(formats = formats + serializer)
  }

  override def register[T<:AnyRef](serializable: DefSerializable[T]): DefSerializable[T] = {
    serializableMap += (serializable.objName -> serializable)
    names += (serializable.runtimeClass -> serializable.objName)
    serializable
  }

  override def register[T<:AnyRef](name: String)(implicit mf: Manifest[T]): DefSerializable[T] = {
    register(JsonSerializable(name, this))
  }

  override def register[T<:AnyRef](implicit mf: Manifest[T]): DefSerializable[T] = {
    register(JsonSerializable(this))
  }

  override def unRegister(name: String): Unit = {
    serializableMap.remove(name)
    names.find(_._2.equals(name)).map(_._1).foreach(names.remove)
  }

  override def unRegister[T<:AnyRef](implicit mf: Manifest[T]): Unit = {
    names.remove(mf.runtimeClass.asInstanceOf[Class[_<:AnyRef]]).foreach(serializableMap.remove)
  }

  import scala.language.existentials
  override def serialize(obj: AnyRef, pretty: Boolean = false): String = {
    val clazz = obj match {
      case _: Throwable => classOf[Throwable]
      case _ => obj.getClass
    }
    val name = names.getOrElse(clazz, clazz.getName)
    val serializer = serializableMap.get(name) match {
      case Some(serializer) =>
        serializer
      case None =>
        if (autoType) register(clazz)
        else throw new IllegalArgumentException("can not serialize obj: " + obj.getClass)
    }
    serializer.toJson(obj).serializedString(pretty)
  }

  override def serializeRaw(obj: AnyRef, pretty: Boolean): String = {
    JackSerial.write(obj)
  }

  override def deSerialize(str: String): AnyRef = {
    val jValue = new JsonObject(JsonMethods.parse(str))
    val name = jValue.objType
    val serializer = serializableMap.get(name) match {
      case Some(serializer) =>
        serializer
      case None =>
        if (autoType) register(Class.forName(name).asInstanceOf[Class[_<:AnyRef]])
        else throw new IllegalArgumentException("can not serialize obj by type: " + name + ", json: " + str)
    }
    serializer.fromJson(jValue)
  }

  override def deSerializeRaw[T<:AnyRef](str: String)(implicit mf: Manifest[T]): T = {
    JackSerial.read(str)(formats, mf)
  }
}