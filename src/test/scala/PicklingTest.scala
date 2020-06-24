import java.net.{InetAddress, UnknownHostException}

/**
 * @author chenmingkun
 * @date 2020/6/23
 */
object PicklingTest {
  def getSelfAddress: String = {
    try {
      InetAddress.getLocalHost.getHostAddress
    } catch {
      case _: UnknownHostException => "127.0.0.1"
      case e: Throwable => throw e
    }
  }

  def main(args: Array[String]): Unit = {
    println(getSelfAddress)
  }
}
