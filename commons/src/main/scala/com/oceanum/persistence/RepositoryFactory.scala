package com.oceanum.persistence

/**
 * @author chenmingkun
 * @date 2020/8/1
 */
trait RepositoryFactory {

  def create[T<:AnyRef](implicit mf: Manifest[T]): Repository[T]

  def expressionFactory: ExpressionFactory
}
