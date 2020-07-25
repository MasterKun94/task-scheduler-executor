package com.oceanum.serialize

import org.json4s._
/**
 * @author chenmingkun
 * @date 2020/7/26
 */
class ThrowableSerializer extends CustomSerializer[Throwable](implicit f =>
  (
    {
      case value: JObject => ???
    },
    {
      case e: Throwable => JObject(
        "message" -> JString(e.getMessage),
        "stacktrace" -> JArray(e.getStackTrace.map(e => Extraction.decompose(e)).toList),

      )
    }
  )
)
