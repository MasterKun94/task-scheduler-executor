package com.oceanum.annotation.util

import com.oceanum.annotation.Injection
import com.oceanum.file.FileClient
import com.oceanum.persistence.{AbstractRepository, Catalog, Repository}
import org.reflections.Reflections

import scala.collection.JavaConversions._
import scala.collection.mutable

object AnnotationUtil {
  private val reflections = new Reflections("com.oceanum")
  private val fileClients = mutable.Set[FileClient]()

  reflections.getTypesAnnotatedWith(classOf[Injection]).foreach { clazz =>
    if (classOf[Repository[_]].isAssignableFrom(clazz)) {
      val repository: Repository[_ <: AnyRef] = clazz.getConstructor().newInstance().asInstanceOf[Repository[_<:AnyRef]]
      Catalog.addRepository(repository)
    } else if (classOf[FileClient].isAssignableFrom(clazz)) {
      val fileClient = clazz.getConstructor().newInstance().asInstanceOf[FileClient]
      fileClients += fileClient
    }
  }

  def main(args: Array[String]): Unit = {
    println(classOf[Repository[_]].isAssignableFrom(classOf[AbstractRepository[_]]))
  }
}