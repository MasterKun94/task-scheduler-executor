import java.io.{BufferedReader, File, FileInputStream, InputStreamReader}
import java.nio.charset.StandardCharsets

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.oceanum.common.Environment

import scala.util.Properties

/**
 * @author chenmingkun
 * @date 2020/6/23
 */
object PicklingTest {
  private implicit lazy val httpSys: ActorSystem = Environment.FILE_SERVER_SYSTEM
  private implicit lazy val httpMat: ActorMaterializer = ActorMaterializer()
  def main(args: Array[String]): Unit = {
//    val source = Source.unfoldResource(
//      create = getStream,
//      read = readLine,
//      close = closeStream
//    )
//    source.runForeach(println)

    import scala.collection.JavaConversions.mapAsScalaMap
    System.getProperties.foreach(println)
  }

  def getStream(): BufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("src/main/resources/application.conf")), StandardCharsets.UTF_8))

  def readLine(inputStream: BufferedReader): Option[String] = {
    Option(inputStream.readLine())
  }

  def closeStream(inputStream: BufferedReader): Unit = inputStream.close()


}
