import com.oceanum.api.entities.WorkflowDefine
import com.oceanum.common.{Environment, SystemInit}
import com.oceanum.persistence.Catalog

import scala.collection.JavaConversions._
import Environment.NONE_BLOCKING_EXECUTION_CONTEXT
import com.oceanum.expr.{ExprParser, JavaHashMap}
import com.oceanum.trigger.Triggers

import scala.util.{Failure, Success}

object TriggerTest {
  def main(args: Array[String]): Unit = {
    Environment.loadEnv(Array("--conf=cluster/src/main/resources/application.properties"))
    SystemInit.initAnnotatedClass()

    Triggers.getTrigger("QUARTZ")
      .start("test", Map(
        "cron" -> "0 * * * * ? *",
        "startTime" -> "${date.format('yyyy-MM-dd HH:mm:ss', date.now() + duration.minute(1))}"
      )) {
        println(_)
      }
  }
}
