package com.oceanum.serialize

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}
import java.util.Base64

import com.fasterxml.jackson.databind.ObjectMapper
import org.json4s._
/**
 * @author chenmingkun
 * @date 2020/7/26
 */
class ThrowableSerializer extends CustomSerializer[Throwable](implicit f => {
  val objectMapper = new ObjectMapper()
  (
    {
      case value: JString =>
        val bytes = Base64.getDecoder.decode(value.values)
        val byteArrayInputStream = new ByteArrayInputStream(bytes)
        val objectInputStream = new ObjectInputStream(byteArrayInputStream)
        val e = objectInputStream.readObject().asInstanceOf[Throwable]
        objectInputStream.close()
        e

      case JNull => null
    },
    {
      case e: Throwable =>
        val byteArrayOutputStream = new ByteArrayOutputStream()
        val objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)
        objectOutputStream.writeObject(e)
        objectOutputStream.flush()
        objectOutputStream.close()
        val bytes = byteArrayOutputStream.toByteArray
        Base64.getEncoder.encodeToString(bytes)
        JString(Base64.getEncoder.encodeToString(bytes))
    }
  )
}
)
