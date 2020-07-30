package com.oceanum.persistence

import scala.concurrent.Future

abstract class AbstractRepository[T<:AnyRef](implicit val mf: Manifest[T]) extends Repository[T] {
  override def save(obj: T): Future[Unit] = ???

  override def findById(idx: String): Future[Option[T]] = ???

  override def find(expr: String): Future[Array[T]] = ???
}
