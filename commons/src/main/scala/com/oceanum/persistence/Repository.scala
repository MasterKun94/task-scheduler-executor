package com.oceanum.persistence

import com.oceanum.persistence.Repository.{User, findById}

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

trait Repository {

  def register[T<:AnyRef](clazz: Class[T])(idx: T => Option[T]): Unit

  def save(obj: AnyRef): Future[Unit]

  def findById[T <: AnyRef](idx: String)(implicit mf: Manifest[T]): Future[Option[T]]

  def find[T <: AnyRef](expr: String)(implicit mf: Manifest[T]): Future[Array[T]]
}

object Repository extends Repository {
  case class User(id: Int, name: String)
  private val map: mutable.Map[String, AnyRef] = mutable.Map("1" -> User(1, "lalalla"))

  override def register[T <: AnyRef](clazz: Class[T])(idx: T => Option[T]): Unit = ???

  override def save(obj: AnyRef): Future[Unit] = ???

  override def findById[T <: AnyRef](idx: String)(implicit mf: Manifest[T]): Future[Option[T]] = {
    println(mf)
    Future.successful(map.get(idx).map(_.asInstanceOf[T]))
  }

  override def find[T <: AnyRef](expr: String)(implicit mf: Manifest[T]): Future[Array[T]] = ???
}

object Main {
  def main(args: Array[String]): Unit = {
    findById[User]("1").onComplete(println)(ExecutionContext.global)
    Thread.sleep(1000)
  }
}