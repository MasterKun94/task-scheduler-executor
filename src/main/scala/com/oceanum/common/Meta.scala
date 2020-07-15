package com.oceanum.common

@SerialVersionUID(1L)
class Meta[T <: Meta[_]](private val map: Map[String, Any]) extends Serializable {
  def +(kv: (String, Any)): T = newInstance(map + (kv._1 -> kv._2))

  def get[V](key: String): Option[V] = map.get(key).map(_.asInstanceOf[V])

  def iterator: Iterator[(String, Any)] = map.iterator

  def -(key: String): T = newInstance(map - key)

  def ++(right: Meta[_]): T = this ++ right.map

  def ++(right: Map[String, Any]): T = newInstance(map ++ right)

  def put(key: String, value: Any): T = this + (key -> value)

  def remove(key: String): T = this - key

  def apply[OUT](key: String): OUT = map.getOrElse(key, null).asInstanceOf[OUT]

  def toMap: Map[String, Any] = map

  private def newInstance(map: Map[String, Any]): T = {
    this.getClass.getConstructor(classOf[Map[String, Any]]).newInstance(map).asInstanceOf[T]
  }
}