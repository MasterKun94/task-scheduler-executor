import com.oceanum.api.entities.WorkflowDefine
import com.oceanum.common.{Environment, SystemInit}
import com.oceanum.persistence.Catalog

import scala.collection.JavaConversions._
import Environment.NONE_BLOCKING_EXECUTION_CONTEXT

import scala.util.{Failure, Success}

object TriggerTest {
  def main(args: Array[String]): Unit = {
    Environment.loadEnv(Array("--conf=cluster/src/main/resources/application.properties"))
    SystemInit.initAnnotatedClass()
    Catalog.getRepository[WorkflowDefine]
      .find("!repo.fieldIn('alive', hosts) && repo.field('name', 'graph-define-test')", Map("hosts" -> Seq(true)))
      .onComplete {
        case Success(value) => value.foreach(println)

        case Failure(exception) => exception.printStackTrace()
      }
  }
}
