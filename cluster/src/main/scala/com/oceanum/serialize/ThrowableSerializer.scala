package com.oceanum.serialize

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}
import java.util.Base64

import com.fasterxml.jackson.databind.ObjectMapper
import org.json4s._
/**
 * @author chenmingkun
 * @date 2020/7/26
 */
class ThrowableSerializer extends CustomSerializer[Throwable](implicit f =>
  (
    {
      case value: JObject =>
        val throwable = Class.forName((value \ "className").extract[String]).getConstructor().newInstance().asInstanceOf[Throwable]
        val e = new RemoteException(
          realClassName = (value \ "className").extract[String],
          message = (value \ "message").extract[String],
          cause = (value \ "cause").extract[Throwable]
        )
        e.setStackTrace((value \ "stackTrace").extract[Array[StackTraceElement]])
        e

      case JNull => null
    },
    {
      case e: Throwable =>
        JObject(
          "className" -> JString(e.getClass.getName),
          "message" -> JString(e.getMessage),
          "cause" -> Extraction.decompose(e.getCause),
          "stackTrace" -> JArray(e.getStackTrace.map(Extraction.decompose).toList)
        )

      case null => JNull
    }
  )
)

class StackTraceElementSerializer extends CustomSerializer[StackTraceElement](implicit f =>
  (
    {
      case value: JObject =>
        new StackTraceElement(
          (value \ "className").extract[String],
          (value \ "methodName").extract[String],
          (value \ "fileName").extract[String],
          (value \ "lineNumber").extract[Int]
        )

      case JNull => null
    },
    {
      case e: StackTraceElement =>
        JObject(
          "className" -> JString(e.getClassName),
          "methodName" -> JString(e.getMethodName),
          "fileName" -> JString(e.getFileName),
          "lineNumber" -> JInt(e.getLineNumber)
        )

      case null => JNull
    }
  )
)

class RemoteException(val realClassName: String, message: String, cause: Throwable)
  extends Exception(message, cause) {
  override def toString: String = {
    "@" + realClassName + ": " + message
  }
}