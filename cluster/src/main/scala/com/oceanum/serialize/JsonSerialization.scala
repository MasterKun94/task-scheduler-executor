package com.oceanum.serialize

import java.util.concurrent.atomic.AtomicBoolean

import com.oceanum.client._
import com.oceanum.common.{GraphContext, RichGraphMeta, RichTaskMeta}
import com.oceanum.exec.State
import com.oceanum.graph.Operators._
import com.oceanum.graph.{FallbackStrategy, GraphDefine, GraphStatus, ReRunStrategy}
import org.json4s.ext.EnumNameSerializer
import org.json4s.jackson.{JsonMethods, Serialization}
import org.json4s.{DefaultFormats, Extraction, Formats, JObject, JString, JValue, Serializer}

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
    register(JsonSerializable(name, clazz))
  }

  def register[T<:AnyRef](clazz: Class[T]): JsonSerializable[T] = {
    register(JsonSerializable(clazz))
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

  def serialize(obj: AnyRef, pretty: Boolean = true): String = {
    val name = names.getOrElse(obj.getClass, obj.getClass.getName)
    val serializer = serializableMap.get(name) match {
      case Some(serializer) =>
        serializer
      case None =>
        if (autoType) register(obj.getClass)
        else throw new IllegalArgumentException("can not serialize obj: " + obj.getClass)
    }
    val jObj = JObject(
      "@type" -> JString(serializer.objName()),
      "value" -> serializer.toJson(obj)
    )
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
    serializer.fromJson(jValue \ "value")
  }
}

object JsonSerialization extends JsonSerialization()(
  DefaultFormats +
    new EnumNameSerializer(FallbackStrategy) +
    new EnumNameSerializer(ReRunStrategy) +
    new EnumNameSerializer(GraphStatus) +
    new EnumNameSerializer(State) +
    new ThrowableSerializer() +
    new StackTraceElementSerializer() +
    TaskSerializer.default() +
    OperatorSerializer.default(),
  mutable.Map.empty[String, JsonSerializable[_<:AnyRef]],
  mutable.Map.empty[Class[_<:AnyRef], String]) {

  private val isInit: AtomicBoolean = new AtomicBoolean(false)

  def init(): Unit = {
    if (isInit.getAndSet(true))
      return

    JsonSerialization.register("GRAPH_META", classOf[RichGraphMeta])
    JsonSerialization.register("TASK_META", classOf[RichTaskMeta])
    JsonSerialization.register("GRAPH_CONTEXT", classOf[GraphContext])
    JsonSerialization.register("TASK", classOf[Task])
    JsonSerialization.register("GRAPH_DEFINE", classOf[GraphDefine])
    JsonSerialization.register("TASK_OPERATOR", classOf[TaskOperator])
    JsonSerialization.register("FORK_OPERATOR", classOf[Fork])
    JsonSerialization.register("JOIN_OPERATOR", classOf[Join])
    JsonSerialization.register("DECISION_OPERATOR", classOf[Decision])
    JsonSerialization.register("CONVERGE_OPERATOR", classOf[Converge])
    JsonSerialization.register("START_OPERATOR", classOf[Start])
    JsonSerialization.register("END_OPERATOR", classOf[End])
  }
}