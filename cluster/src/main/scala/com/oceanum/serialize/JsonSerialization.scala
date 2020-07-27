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
                        protected val serializers: mutable.Map[String, JsonSerializable[_<:AnyRef]] = mutable.Map(),
                        protected val names: mutable.Map[Class[_<:AnyRef], String] = mutable.Map()) {


  def withJsonSerializer[T](serializer: Serializer[T]): JsonSerialization = {
    new JsonSerialization()(formats = formats + serializer, serializers, names)
  }

  def register[T<:AnyRef](serializable: JsonSerializable[T]): Unit = {
    serializers += (serializable.objName -> serializable)
    names += (serializable.objIdentifier() -> serializable.objName())
  }

  def register[T<:AnyRef](name: String, clazz: Class[T]): Unit = {
    val serializable = new JsonSerializable[T] {
      override def toJson(obj: AnyRef): JValue = Extraction.decompose(obj)

      override def fromJson(value: JValue): T = value.extract(formats, Manifest.classType(clazz))

      override def objName(): String = name

      override def objIdentifier(): Class[T] = clazz
    }
    register(serializable)
  }

  def register[T<:AnyRef](clazz: Class[T]): Unit = {
    register(clazz.getName, clazz)
  }

  def unRegister[T<:AnyRef](clazz: Class[T]): Unit = {
    names.remove(clazz).foreach {
      serializers.remove
    }
  }

  def unRegister(name: String): Unit = {
    serializers.remove(name)
    names.find(_._2.equals(name)).map(_._1).foreach(names.remove)
  }

  def serialize(obj: AnyRef, pretty: Boolean = true): String = {
    val name = names(obj.getClass)
    val serializer = serializers(name)
    val jObj = JObject(
      "@type" -> JString(serializer.objName()),
      "value" -> serializer.toJson(obj)
    )
    if (pretty)
      Serialization.writePretty(jObj)
    else
      Serialization.write(jObj)
  }

  def deSerialize(str: String): AnyRef = {
    val jValue = JsonMethods.parse(str)
    val name = (jValue \ "@type").extract[String]
    val serializer = serializers(name)
    serializer.fromJson(jValue \ "value")
  }

  override def toString: String = s"JsonSerializer(${serializers}, ${names})"
}

object JsonSerialization extends JsonSerialization()(
  DefaultFormats +
    new EnumNameSerializer(FallbackStrategy) +
    new EnumNameSerializer(ReRunStrategy) +
    new EnumNameSerializer(GraphStatus) +
    new EnumNameSerializer(State) +
    new ThrowableSerializer() +
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

  private def createTaskSerializable[T<:TaskProp](name: String)(kvs: (String, Class[_<:T])*): JsonSerializable[Task] = new JsonSerializable[Task] {
    val propClasses: Map[String, Class[_<:T]] = Map(kvs: _*)

    override def toJson(t: AnyRef): JValue = Extraction.decompose(t) merge JObject("taskType" -> JString(t.asInstanceOf[Task].prop.taskType))

    override def fromJson(value: JValue): Task = {
      val taskType = (value \ "taskType").extract[String]
      val taskProp: TaskProp = (value \ "prop").extract(formats, Manifest.classType(propClasses(taskType)))
      value
        .removeField(_._1.equals("prop"))
        .extract[Task]
        .copy(prop = taskProp)
    }

    override def objIdentifier(): Class[Task] = classOf[Task]

    override def objName(): String = "TASK_" + name
  }
}