package com.oceanum.persistence

import scala.concurrent.Future

trait Repository[T<:AnyRef] {
  def manifest: Manifest[T]

  def save(id: String, obj: T): Future[Unit]

  def findById(id: String): Future[Option[T]]

  def find(expr: String): Future[Array[T]]
}