package com.oceanum.Test

object Main {

  def main(args: Array[String]): Unit = {
    com.oceanum.pluggable.Main.main(Array("com.oceanum.pluggable.DemoExecutor", "akka.tcp://cluster@127.0.0.1:4551/user/$a#612438235", "192.168.10.55", "test"))

  }

//  def main(args: Array[String]): Unit = {
//    import Environment.NONE_BLOCKING_EXECUTION_CONTEXT
//    import scala.collection.JavaConversions.mapAsJavaMap
//    Environment.loadEnv(Array("--conf=cluster/src/main/resources/application.properties"))
//    Environment.printEnv()
//    Environment.initSystem()
//    val repo = Catalog.getRepository[GraphMeta]
//    val env = Map("name" -> "7bf47817-0b76-4a1e-a53a-c4e7c6b38f2a")
//    repo.find("repo.select(repo.field('name', name), repo.sort('rerunId', 'DESC'))", env).onComplete {
//      case Success(value) =>
//        println("---------")
//        value.foreach(println)
//      case Failure(exception) => exception.printStackTrace()
//    }
//  }
}
