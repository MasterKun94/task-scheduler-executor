import scala.util.Properties

object Test {
  def main(args: Array[String]): Unit = {
    println(Properties.javaClassPath)

    import scala.sys.process._
    val process = Process("").run()
    process.destroy()
  }
}
