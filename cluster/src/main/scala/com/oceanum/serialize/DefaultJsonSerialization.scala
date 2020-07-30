package com.oceanum.serialize

import com.oceanum.common._
import org.json4s.DefaultFormats
import org.json4s.ext.EnumNameSerializer

object DefaultJsonSerialization extends JsonSerialization()(
  DefaultFormats +
    new EnumNameSerializer(FallbackStrategy) +
    new EnumNameSerializer(ReRunStrategy) +
    new EnumNameSerializer(GraphStatus) +
    new EnumNameSerializer(TaskStatus) +
    new ThrowableSerializer() +
    new StackTraceElementSerializer() +
    TaskSerializer.default() +
    OperatorSerializer.default()) {
}