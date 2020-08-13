package com.oceanum.util

import scala.collection.JavaConversions.mapAsJavaMap

case class MapReduceArgs(conf: Map[String, String]) {

  def getConf: java.util.Map[String, String] = conf

}

object MapReduceArgs {
  def fromJson(json: String): MapReduceArgs = ???

  def toJson(mapReduceArgs: MapReduceArgs): String = ???
}
