package com.oceanum.persistence

import scala.concurrent.Future

trait Repository[T<:AnyRef] {
  def manifest: Manifest[T]

  def save(id: String, obj: T): Future[Unit]

  def saveAll(objs: Seq[(String, T)]): Future[Unit]

  def findById(id: String): Future[Option[T]]

  def findByIdIn(ids: Seq[String]): Future[Seq[T]]

  def find(expr: String): Future[Seq[T]]
}