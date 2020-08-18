package com.oceanum.jdbc.persistence

import com.oceanum.annotation.IRepositoryFactory
import com.oceanum.jdbc.expr.{JdbcExpressionFactory, SelectExpression}
import com.oceanum.persistence.{ExpressionFactory, Repository, RepositoryFactory}

import scala.reflect.runtime.universe._
import scala.reflect.{ClassTag, ManifestFactory}

@IRepositoryFactory(priority = 100)
class JdbcRepositoryFactory extends RepositoryFactory {
  override def create[T <: AnyRef](implicit tag: TypeTag[T]): Repository[T] = ???

  override def expressionFactory: ExpressionFactory = new JdbcExpressionFactory()
}

object JdbcRepositoryFactory {
  def main(args: Array[String]): Unit = {
//    Environment.loadEnv(Array("--conf=cluster/src/main/resources/application.properties"))
//    Environment.initSystem()
//    val env = new JavaHashMap[String, AnyRef]()
//    env.put("sorts", Array(Sort("id", "DESC"), Sort("col", "ASC")))
//    val expr = """
//                 |repo.select(
//                 | repo.field('name') == 'name',
//                 | repo.sort(sorts),
//                 | repo.size(10),
//                 | repo.page(0)
//                 |)
//                 |""".stripMargin
//    val obj = Evaluator.rawExecute(expr, env)
//    implicit val sql: Sql = new Sql()
//    implicit val t: Table = table("test")
//    println(obj.asInstanceOf[SelectExpression].createSql[SelectExpression])


    def toManifest[T](implicit tag: TypeTag[T]): Manifest[T] = {
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

    println(toManifest[SelectExpression].runtimeClass)

  }
}
