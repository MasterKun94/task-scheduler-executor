package com.oceanum.persistence

import com.oceanum.expr.{JavaHashMap, JavaMap}

import scala.concurrent.Future

/**
 * 对象仓库，用于持久化和查询对象
 * 如果要自定义的话实现以下这几个类：
 *    {@link com.oceanum.api.entities.WorkflowMetaInfo}
 *    {@link com.oceanum.api.entities.TaskMetaInfo}
 *    {@link com.oceanum.api.entities.Coordinator}
 *    {@link com.oceanum.api.entities.WorkflowDefine}
 *    {@link com.oceanum.api.entities.CoordinatorLog}
 *    {@link com.oceanum.api.entities.CoordinatorStatus}
 * 然后添加注解：
 *    {@link com.oceanum.annotation.IRepository}
 *
 * 或者直接实现仓库工厂：
 *    {@link com.oceanum.persistence.RepositoryFactory}
 * 并添加注解：
 *    {@link com.oceanum.annotation.IRepositoryFactory}
 *
 * 此外还需要实现 com/oceanum/expr/RepoFunction.scala下的所有抽象类，用于支持仓库的查询表达式
 */
trait Repository[T<:AnyRef] {
  def manifest: Manifest[T]

  def save(id: String, obj: T): Future[Unit]

  def save(obj: T): Future[String]

  def saveAll(objs: Seq[(String, T)]): Future[Unit]

  def findById(id: String): Future[Option[T]]

  def findByIdIn(ids: Seq[String]): Future[Seq[T]]

  /**
   * 使用表达式查询，自定义表达式参考 {@link com.googlecode.aviator.runtime.function.AbstractFunction}
   *
   * @param expr 表达式
   * @param env 参数
   * @return 查询结果
   */
  def find(expr: String, env: JavaMap[String, AnyRef]): Future[Seq[T]]
}