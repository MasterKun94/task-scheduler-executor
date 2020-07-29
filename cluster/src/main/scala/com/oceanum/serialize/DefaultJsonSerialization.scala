package com.oceanum.serialize

import java.util.concurrent.atomic.AtomicBoolean

import com.oceanum.client._
import com.oceanum.common.{FallbackStrategy, GraphContext, GraphStatus, ReRunStrategy, RichGraphMeta, RichTaskMeta, TaskStatus}
import com.oceanum.exec.State
import com.oceanum.graph.Operators._
import com.oceanum.graph.GraphDefine
import org.json4s.ext.EnumNameSerializer
import org.json4s.jackson.{JsonMethods, Serialization}
import org.json4s.{DefaultFormats, Extraction, Formats, JObject, JString, JValue, Serializer}

import scala.collection.mutable


object DefaultJsonSerialization extends JsonSerialization()(
  DefaultFormats +
    new EnumNameSerializer(FallbackStrategy) +
    new EnumNameSerializer(ReRunStrategy) +
    new EnumNameSerializer(GraphStatus) +
    new EnumNameSerializer(TaskStatus) +
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

    DefaultJsonSerialization.register("GRAPH_META", classOf[RichGraphMeta])
    DefaultJsonSerialization.register("TASK_META", classOf[RichTaskMeta])
    DefaultJsonSerialization.register("GRAPH_CONTEXT", classOf[GraphContext])
    DefaultJsonSerialization.register("TASK", classOf[Task])
    DefaultJsonSerialization.register("GRAPH_DEFINE", classOf[GraphDefine])
    DefaultJsonSerialization.register("TASK_OPERATOR", classOf[TaskOperator])
    DefaultJsonSerialization.register("FORK_OPERATOR", classOf[Fork])
    DefaultJsonSerialization.register("JOIN_OPERATOR", classOf[Join])
    DefaultJsonSerialization.register("DECISION_OPERATOR", classOf[Decision])
    DefaultJsonSerialization.register("CONVERGE_OPERATOR", classOf[Converge])
    DefaultJsonSerialization.register("START_OPERATOR", classOf[Start])
    DefaultJsonSerialization.register("END_OPERATOR", classOf[End])
  }
}