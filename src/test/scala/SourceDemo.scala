import com.oceanum.file.{FileClient, FileServer}
import com.oceanum.metrics.MetricsListener
import com.oceanum.utils.Test

import scala.concurrent.ExecutionContext.Implicits._
import scala.util.{Failure, Success}
import com.oceanum.client.Implicits.PathHelper

object SourceDemo extends App {
  val path = "C:/Users/chenmingkun/work/idea/work/task-scheduler-core/task-scheduler-executor/src/main/resources/"

  Test.startCluster(args)
//  MetricsListener.start()

  FileServer.start().onComplete {
    case Success(_) =>
      FileClient.download(Test.ip, path, path/"file"/).onComplete(println)
      FileClient.upload(Test.ip, path/"test.py", path/ "file"/"test2.py").onComplete(println)
      Thread.sleep(2000)
      FileClient.transfer(Test.ip, path/"file", Test.ip, path/"file2").onComplete(println)
      Thread.sleep(2000)
      FileClient.delete(Test.ip, path/"file"/"application.properties").onComplete(println)

    case Failure(e) => e.printStackTrace()
  }

  Thread.sleep(2000)
  Test.startClient(args)
}
