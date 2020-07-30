package com.oceanum.serialize

import org.json4s.Formats
import org.json4s.JsonAST.JValue
import org.json4s.jackson.{Serialization => JackSerial}

class JsonObject(value: JValue)(implicit formats: Formats) extends WrappedObject {
  override def objType: String = (value \ "@type").extract[String]

  override def serializedString[T<:AnyRef](pretty: Boolean = false): String = {
    if (pretty) {
      JackSerial.writePretty(value)
    } else {
      JackSerial.write(value)
    }
  }

  override def deserializedObject[T<:AnyRef](mf: Manifest[T]): T = {
    value.extract(formats, mf)
  }
}