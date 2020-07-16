import com.oceanum.common.Environment
import com.oceanum.file.{ClusterFileServer, ClusterFileServerApi, FileClient}
import com.oceanum.metrics.MetricsListener
import com.oceanum.utils.Test

import scala.concurrent.ExecutionContext.Implicits._
import scala.util.{Failure, Success}
import com.oceanum.common.Implicits.PathHelper

object SourceDemo extends App {
  val path = "C:/Users/chenmingkun/work/idea/work/task-scheduler-core/task-scheduler-executor/src/main/resources/"

  Test.startCluster(args)
//  MetricsListener.start()

  ClusterFileServer.start().onComplete {
    case Success(_) =>
//      ClusterFileServerApi.download(Test.ip, path, path/"file").onComplete(println)
//      ClusterFileServerApi.upload(Test.ip, path/"python-err.py", path/ "file"/"test2.py").onComplete(println)
//      Thread.sleep(2000)
//      ClusterFileServerApi.transfer(Test.ip, path/"file", Test.ip, path/"file2").onComplete(println)
//      Thread.sleep(2000)
//      ClusterFileServerApi.delete(Test.ip, path/"file"/"application.properties").onComplete(println)
    FileClient.download("cluster://"/Environment.BASE_PATH/"src"/"main"/"resources", Environment.BASE_PATH/"src"/"main"/"resources"/"test").onComplete(_ => println("complete"))

    //
    case Failure(e) => e.printStackTrace()
  }


  Thread.sleep(2000)
//  Test.startClient(args)
}
