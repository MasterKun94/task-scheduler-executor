package com.oceanum.persistence

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future

object Catalog {
  private val repositories: TrieMap[Manifest[_], Repository[_]] = TrieMap()

  def addRepository[T<:AnyRef](repository: Repository[T])(implicit mf: Manifest[T]): Unit = {
    repositories += (mf -> repository)
  }

  def removeRepository[T<:AnyRef](implicit mf: Manifest[T]): Option[Repository[T]] = {
    repositories.remove(mf).map(_.asInstanceOf[Repository[T]])
  }

  def getRepository[T<:AnyRef](implicit mf: Manifest[T]): Repository[T] = {
    repositories(mf).asInstanceOf[Repository[T]]
  }

  def save[T<:AnyRef](obj: T)(implicit mf: Manifest[T]): Future[Unit] = getRepository(mf).save(obj)

  def findById[T<:AnyRef](idx: String)(implicit mf: Manifest[T]): Future[Option[T]] = getRepository(mf).findById(idx)

  def find[T<:AnyRef](expr: String)(implicit mf: Manifest[T]): Future[Array[T]] = getRepository(mf).find(expr)
}
