import com.oceanum.pluggable.Main

object PlugTest {
  def main(args: Array[String]): Unit = {
    val args0 = Array("com.oceanum.pluggable.DemoExecutor", "akka.tcp://cluster@192.168.10.131:4551/user/$a#737061832", "192.168.10.55", "test")

    Main.main(Array("com.oceanum.pluggable.DemoExecutor", "akka.tcp://cluster@192.168.10.131:4551/user/$a#737061832", "192.168.10.55", "test"))
  }
}
