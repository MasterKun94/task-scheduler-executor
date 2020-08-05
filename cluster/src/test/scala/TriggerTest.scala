import com.oceanum.common.{Environment, RichGraphMeta, SystemInit}
import com.oceanum.triger.Triggers

object TriggerTest {
  def main(args: Array[String]): Unit = {
    Environment.loadEnv(Array("--conf=cluster/src/main/resources/application.properties"))
    SystemInit.initAnnotatedClass()
    val serialization = SystemInit.serialization
    val str = serialization.serialize(new RichGraphMeta())
    println(serialization.deSerializeRaw[String](str))
  }
}
