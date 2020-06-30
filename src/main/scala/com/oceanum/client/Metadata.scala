package com.oceanum.client

case class Metadata(map: Map[String, String]) extends Map[String, String] {
  override def +[B1 >: String](kv: (String, B1)): Metadata = Metadata(map + (kv._1 -> kv._2.asInstanceOf[String]))

  override def get(key: String): Option[String] = map.get(key)

  override def iterator: Iterator[(String, String)] = map.iterator

  override def -(key: String): Metadata = Metadata(map - key)
}

object Metadata {

  def empty: Metadata = new Metadata(Map.empty)

  def apply(kv: (String, String)*): Metadata = new Metadata(Map(kv: _*))
}
