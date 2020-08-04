import com.oceanum.common.{Environment, SystemInit}
import com.oceanum.triger.Triggers

object TriggerTest {
  def main(args: Array[String]): Unit = {
    Environment.loadEnv(Array("--conf=cluster/src/main/resources/application.properties"))
    SystemInit.initAnnotatedClass()
    val trigger = Triggers.getTrigger("quartz")
    trigger.start("test1", Map("cron" -> "*/5 * * ? * *")) { date =>
      println("hello")
    }
  }
}
