package com.oceanum.persistence

import com.oceanum.common.SystemInit

import scala.collection.concurrent.TrieMap
import scala.reflect.runtime.universe.TypeTag

object Catalog {
  private val repositories: TrieMap[TypeTag[_], Repository[_]] = TrieMap()
  private lazy val repositoryFactory: RepositoryFactory = SystemInit.repositoryFactory

  def registerRepository[T<:AnyRef](repository: Repository[T]): Unit = {
    repositories += (repository.typeTag -> repository)
  }

  def unregisterRepository[T<:AnyRef](implicit tag: TypeTag[T]): Option[Repository[T]] = {
    repositories.remove(tag).map(_.asInstanceOf[Repository[T]])
  }

  def getRepository[T<:AnyRef](implicit mf: TypeTag[T]): Repository[T] = {
    repositories.getOrElseUpdate(mf, repositoryFactory.create(mf)).asInstanceOf[Repository[T]]
  }
}
