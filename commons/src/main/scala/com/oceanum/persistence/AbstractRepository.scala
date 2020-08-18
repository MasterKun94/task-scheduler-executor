package com.oceanum.persistence
import scala.reflect.runtime.universe

abstract class AbstractRepository[T<:AnyRef](implicit protected val tag: universe.TypeTag[T]) extends Repository[T] {
  override def typeTag: universe.TypeTag[T] = tag
}
