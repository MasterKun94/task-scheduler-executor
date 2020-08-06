package com.oceanum.persistence

import com.oceanum.common.SystemInit

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future

object Catalog {
  private val repositories: TrieMap[Manifest[_], Repository[_]] = TrieMap()
  private lazy val repositoryFactory: RepositoryFactory = SystemInit.repositoryFactory

  def addRepository[T<:AnyRef](repository: Repository[T]): Unit = {
    repositories += (repository.manifest -> repository)
  }

  def removeRepository[T<:AnyRef](implicit mf: Manifest[T]): Option[Repository[T]] = {
    repositories.remove(mf).map(_.asInstanceOf[Repository[T]])
  }

  def getRepository[T<:AnyRef](implicit mf: Manifest[T]): Repository[T] = {
    repositories.getOrElseUpdate(mf, repositoryFactory.create(mf)).asInstanceOf[Repository[T]]
  }

  def save[T<:AnyRef](id: String, obj: T)(implicit mf: Manifest[T]): Future[Unit] = getRepository(mf).save(id, obj)

  def saveAll[T<:AnyRef](objs: Seq[(String, T)])(implicit mf: Manifest[T]): Future[Unit] = getRepository(mf).saveAll(objs)

  def findById[T<:AnyRef](id: String)(implicit mf: Manifest[T]): Future[Option[T]] = getRepository(mf).findById(id)

  def findByIdIn[T<:AnyRef](ids: Seq[String])(implicit mf: Manifest[T]): Future[Seq[T]] = getRepository(mf).findByIdIn(ids)

  def find[T<:AnyRef](expr: String)(implicit mf: Manifest[T]): Future[Seq[T]] = getRepository(mf).find(expr)
}
