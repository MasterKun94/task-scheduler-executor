package com.oceanum.util

import java.util.Optional

import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization
import scala.compat.java8.OptionConverters._

case class FlinkArgs(clientAddress: Option[String],
                    jar: String,
                    mainClass: String,
                    args: Array[String]
                    ) {
  def getClientAddress: Optional[String] = clientAddress.asJava
}

object FlinkArgs {
  implicit val formats: DefaultFormats = DefaultFormats
  def fromJson(json: String): FlinkArgs = {
    Serialization.read[FlinkArgs](json)
  }
  def toJson(flinkArgs: FlinkArgs): String = {
    Serialization.write(flinkArgs)
  }
}
