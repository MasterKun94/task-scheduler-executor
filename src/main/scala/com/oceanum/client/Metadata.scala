package com.oceanum.client


case class Metadata(map: Map[String, String]) {
  def +(kv: (String, String)): Metadata = Metadata(map + (kv._1 -> kv._2))

  def get(key: String): Option[String] = map.get(key)

  def iterator: Iterator[(String, String)] = map.iterator

  def -(key: String): Metadata = Metadata(map - key)

  def ++(right: Metadata): Metadata = this ++ right.map

  def ++(right: Map[String, String]): Metadata = new Metadata(map ++ right)

  def put(key: String, value: String): Metadata = this + (key -> value)

  def remove(key: String): Metadata = this - key

  def apply(key: String): String = map(key)

  def toMap: Map[String, String] = map

  override def toString: String = s"Metadata(${map.toArray.map(t => t._1 + ": " + t._2).mkString(", ")})"
}

object Metadata {

  def empty: Metadata = new Metadata(Map.empty)

  def apply(kv: (String, String)*): Metadata = new Metadata(Map(kv: _*))
}
