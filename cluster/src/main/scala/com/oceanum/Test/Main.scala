package com.oceanum.Test

import com.oceanum.common.{Environment, GraphMeta}
import com.oceanum.persistence.Catalog

import scala.util.{Failure, Success}

object Main {

  def main(args: Array[String]): Unit = {
    import Environment.NONE_BLOCKING_EXECUTION_CONTEXT
    import scala.collection.JavaConversions.mapAsJavaMap
    Environment.loadEnv(Array("--conf=cluster/src/main/resources/application.properties"))
    Environment.printEnv()
    Environment.initSystem()
    val repo = Catalog.getRepository[GraphMeta]
    val env = Map("name" -> "7bf47817-0b76-4a1e-a53a-c4e7c6b38f2a")
    repo.find("repo.select(repo.field('name', name), repo.sort('rerunId', 'DESC'))", env).onComplete {
      case Success(value) =>
        println("---------")
        value.foreach(println)
      case Failure(exception) => exception.printStackTrace()
    }
  }
}
