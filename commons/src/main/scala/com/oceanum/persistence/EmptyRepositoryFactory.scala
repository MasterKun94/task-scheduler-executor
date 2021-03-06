package com.oceanum.persistence

import com.oceanum.annotation.IRepositoryFactory
import scala.reflect.runtime.universe.TypeTag
/**
 * @author chenmingkun
 * @date 2020/8/1
 */
@IRepositoryFactory(priority = -1)
class EmptyRepositoryFactory extends RepositoryFactory {
  override def create[T <: AnyRef](implicit mf: TypeTag[T]): Repository[T] = {
    throw new IllegalArgumentException("no RepositoryFactory found")
  }

  override def expressionFactory: ExpressionFactory = ???
}
