package com.oceanum.util

import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization

import scala.collection.JavaConversions.mapAsJavaMap
import scala.compat.java8.OptionConverters._

case class SparkArgs(appName: Option[String],
                     appResource: String,
                     mainClass: String,
                     appArgs: Array[String] = Array.empty,
                     sparkHome: String = "",
                     hadoopHome: String = "",
                     master: String = "local[2]",
                     deployMode: Option[String] = None,
                     conf: Map[String, String] = Map.empty,
                     jars: Array[String] = Array.empty,
                     files: Array[String] = Array.empty,
                     pyFiles: Array[String] = Array.empty,
                     propertiesFile: Option[String] = None) {
  def getConf: java.util.Map[String, String] = conf
  def getAppName: java.util.Optional[String] = appName.asJava
  def getDeployMode: java.util.Optional[String] = deployMode.asJava
  def getPropertiesFile: java.util.Optional[String] = propertiesFile.asJava
}

object SparkArgs {
  implicit val formats: DefaultFormats = DefaultFormats
  def fromJson(json: String): SparkArgs = {
    Serialization.read[SparkArgs](json)
  }
  def toJson(sparkArgs: SparkArgs): String = {
    Serialization.write(sparkArgs)
  }
}