package com.oceanum.jdbc.persistence

import com.oceanum.expr.JavaMap
import com.oceanum.jdbc.expr.GtExpression

import scala.concurrent.Future
import scala.reflect.runtime.universe._

class JdbcUtil {



  def find[T](table: String, expr: String, env: JavaMap[String, AnyRef])(implicit tag: TypeTag[T]): Future[Seq[T]] = {
    Manifest.classType(classOf[GtExpression]).typeArguments
    ???
  }
}
