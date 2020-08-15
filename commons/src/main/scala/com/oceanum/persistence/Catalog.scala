package com.oceanum.persistence

import com.oceanum.common.SystemInit
import com.oceanum.expr.JavaHashMap

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
}
