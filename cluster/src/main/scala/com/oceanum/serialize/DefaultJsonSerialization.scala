package com.oceanum.serialize

import com.oceanum.annotation.ISerialization

@ISerialization(priority = 1)
class DefaultJsonSerialization extends JsonSerialization()(
  JsonSerialization.formats +
    TaskSerializer.default() +
    OperatorSerializer.default()) {
}