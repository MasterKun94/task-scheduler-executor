package com.oceanum.client


case class Metadata(map: Map[String, Any]) {
  def +(kv: (String, Any)): Metadata = Metadata(map + (kv._1 -> kv._2))

  def get(key: String): Option[Any] = map.get(key)

  def iterator: Iterator[(String, Any)] = map.iterator

  def -(key: String): Metadata = Metadata(map - key)

  def ++(right: Metadata): Metadata = this ++ right.map

  def ++(right: Map[String, Any]): Metadata = new Metadata(map ++ right)

  def put(key: String, value: Any): Metadata = this + (key -> value)

  def remove(key: String): Metadata = this - key

  def apply[OUT](key: String): OUT = map(key).asInstanceOf[OUT]

  def toMap: Map[String, Any] = map

  override def toString: String = s"Metadata(${map.toArray.map(t => t._1 + ": " + t._2).mkString(", ")})"
}

object Metadata {

  def empty: Metadata = new Metadata(Map.empty)

  def apply(kv: (String, String)*): Metadata = new Metadata(Map(kv: _*))
}
