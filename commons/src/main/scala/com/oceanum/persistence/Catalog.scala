package com.oceanum.persistence

import com.oceanum.api.entities.{Coordinator, CoordinatorLog, CoordinatorStatus, TaskMetaInfo, WorkflowDefine, WorkflowMetaInfo}
import com.oceanum.common.{GraphMeta, SystemInit, TaskMeta}
import com.oceanum.jdbc.expr.JavaHashMap

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future

object Catalog {
  private val repositories: TrieMap[Manifest[_], Repository[_]] = TrieMap()
  private lazy val repositoryFactory: RepositoryFactory = SystemInit.repositoryFactory

  def registerRepository[T<:AnyRef](repository: Repository[T]): Unit = {
    repositories += (repository.manifest -> repository)
  }

  def unregisterRepository[T<:AnyRef](implicit mf: Manifest[T]): Option[Repository[T]] = {
    repositories.remove(mf).map(_.asInstanceOf[Repository[T]])
  }

  def getRepository[T<:AnyRef](implicit mf: Manifest[T]): Repository[T] = {
    repositories.getOrElseUpdate(mf, repositoryFactory.create(mf)).asInstanceOf[Repository[T]]
  }
}
