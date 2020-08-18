import com.oceanum.common.Environment
import com.oceanum.trigger.Triggers

object TriggerTest {
  def main(args: Array[String]): Unit = {
    Environment.loadEnv(Array("--conf=cluster/src/main/resources/application.properties"))
    Environment.initSystem()

    Triggers.getTrigger("QUARTZ")
      .start("test", Map(
        "cron" -> "0 * * * * ? *"
      ), startTime = None) {
        println(_, _)
      }
  }
}
