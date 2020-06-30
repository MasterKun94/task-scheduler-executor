package com.oceanum.client

class Metadata(map: Map[String, String]) extends Map[String, String] {
  override def +[B1 >: String](kv: (String, B1)): Map[String, B1] = map + kv

  override def get(key: String): Option[String] = map.get(key)

  override def iterator: Iterator[(String, String)] = map.iterator

  override def -(key: String): Map[String, String] = map - key
}

object Metadata {
  def apply(map: Map[String, String]): Metadata = new Metadata(map)

  def empty: Metadata = new Metadata(Map.empty)

  def apply(kv: (String, String)*): Metadata = new Metadata(Map(kv: _*))
}
