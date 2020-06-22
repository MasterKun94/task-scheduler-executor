package scala

import java.io.File

import akka.util.Timeout
import com.oceanum.ClusterStarter
import com.oceanum.api.{LineHandler, PythonTaskProp, SchedulerClient, Task}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.util.{Failure, Success}
import com.oceanum.api.Implicits._


/**
 * @author chenmingkun
 * @date 2020/6/18
 */
object Test {
  def startCluster(args: Array[String]): Unit = {
    ClusterStarter.main(Array("--port=3551", "--topics=t1,a1", "--host=192.168.10.55", "--seed-node=192.168.10.55:3551", "--conf=src\\main\\resources\\application.properties,src\\main\\resources\\application-env.properties"))
//    ClusterStarter.main(Array("--port=3552", "--topics=t2,a2", "--conf=application.properties,application-env.properties"))
//    ClusterStarter.main(Array("--port=3553", "--topics=t3,a3", "--conf=application.properties,application-env.properties"))
  }


  def startClient(args: Array[String]): Unit = {
    //    val path = "C:\\Users\\chenmingkun\\work\\idea\\work\\task-scheduler-core\\task-scheduler-executor\\src\\main\\resources"
    val path = "E:\\chenmingkun\\task-scheduler-executor\\src\\main\\resources"
    implicit val executionContext: ExecutionContextExecutor = ExecutionContext.global
    implicit val timeout: Timeout = fd"10 second"
    implicit val timeWait: FiniteDuration = fd"2 second"
    val client = SchedulerClient.create
    val future = client
      .executeAll("test", Task(
        "test",
        3,
        3000,
        1,
        PythonTaskProp(
          pyFile = s"$path\\test.py",
          args = Array("hello", "world"),
          env = Map("FILE_NAME" -> "test.py"),
          stdoutLineHandler = () => LineHandler.fileOutputHandler(new File(s"$path\\test1.txt")),
          stderrLineHandler = () => LineHandler.fileOutputHandler(new File(s"$path\\test2.txt")),
          //          stdoutLineHandler = LineHandlerProducer.print(),
          //          stderrLineHandler = LineHandlerProducer.print(),
          waitForTimeout = 100000
        )))
    future
      .onComplete {
        case Success(value) =>
          value.handleState(fd"2s", state => println("state is: " + state))
          value.onComplete(stat => {
            println("result is: " + stat)
            value.close()
          })
          Thread.sleep(12000)
          value.kill()
        case Failure(exception) =>
          exception.printStackTrace()
      }
    Thread.sleep(100000)
    SchedulerClient.terminate()
  }

  def main(args: Array[String]): Unit = {
//    startCluster(args)
    Thread.sleep(2000)
    startClient(args)
  }
}
