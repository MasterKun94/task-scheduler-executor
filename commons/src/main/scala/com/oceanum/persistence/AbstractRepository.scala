package com.oceanum.persistence

abstract class AbstractRepository[T<:AnyRef](implicit protected val mf: Manifest[T]) extends Repository[T] {
  override def manifest: Manifest[T] = mf
}
