package com.oceanum.persistence

import scala.reflect.{ClassTag, ManifestFactory}
import scala.reflect.runtime.universe._
/**
 * @author chenmingkun
 * @date 2020/8/1
 */
trait RepositoryFactory {

  def create[T<:AnyRef](implicit tag: TypeTag[T]): Repository[T]

  def expressionFactory: ExpressionFactory

  def manifest[T<:AnyRef](implicit tag: TypeTag[T]): Manifest[T] = {
    val mirror = tag.mirror
    def toManifestRec(t: Type): Manifest[_] = {
      val clazz = ClassTag[T](mirror.runtimeClass(t)).runtimeClass
      if (t.typeArgs.length == 1) {
        val arg = toManifestRec(t.typeArgs.head)
        ManifestFactory.classType(clazz, arg)
      } else if (t.typeArgs.length > 1) {
        val args = t.typeArgs.map(x => toManifestRec(x))
        ManifestFactory.classType(clazz, args.head, args.tail: _*)
      } else {
        ManifestFactory.classType(clazz)
      }
    }
    toManifestRec(tag.tpe).asInstanceOf[Manifest[T]]
  }
}
