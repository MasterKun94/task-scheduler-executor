package com.oceanum.serialize

import com.oceanum.annotation.ISerialization
import com.oceanum.common.{CoordStatus, FallbackStrategy, GraphStatus, NodeStatus, RerunStrategy, TaskStatus}
import org.json4s.DefaultFormats

@ISerialization(priority = 1)
class DefaultJsonSerialization extends JsonSerialization()(
  DefaultFormats +
    new EnumSerializer[GraphStatus]() +
    new EnumSerializer[TaskStatus]() +
    new EnumSerializer[CoordStatus]() +
    new EnumSerializer[RerunStrategy]() +
    new EnumSerializer[FallbackStrategy]() +
    new EnumSerializer[NodeStatus]() +
    new ThrowableSerializer() +
    new StackTraceElementSerializer() +
    TaskSerializer.default() +
    VertexSerializer.default()) {
}