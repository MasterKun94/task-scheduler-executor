package com.oceanum.utils

import java.io.File

import akka.util.Timeout
import com.oceanum.ClusterStarter
import com.oceanum.api.{LineHandler, PythonTaskProp, SchedulerClient, Task, TaskPropBuilder}
import com.oceanum.common.Environment
import com.oceanum.common.Environment.WINDOWS

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.util.{Failure, Success}

/**
 * @author chenmingkun
 * @date 2020/6/18
 */
object Test {
  def startCluster(args: Array[String]): Unit = {
    ClusterStarter.main(Array("--port=3551", "--topics=t1,a1", "--host=127.0.0.1", "--seed-node=127.0.0.1", "--conf=src\\main\\resources\\application.properties,src\\main\\resources\\application-env.properties"))
//    ClusterStarter.main(Array("--port=3552", "--topics=t2,a2", "--conf=application.properties,application-env.properties"))
//    ClusterStarter.main(Array("--port=3553", "--topics=t3,a3", "--conf=application.properties,application-env.properties"))
  }


  def startClient(args: Array[String]): Unit = {
    import com.oceanum.api.Implicits._
    val path = "C:\\Users\\chenmingkun\\work\\idea\\work\\task-scheduler-core\\task-scheduler-executor\\src\\main\\resources"
    val path2 = "E:\\chenmingkun\\task-scheduler-executor\\src\\main\\resources"
//    val path = "/root/test"
    implicit val executionContext: ExecutionContextExecutor = ExecutionContext.global
    implicit val timeout: Timeout = fd"10 second"
    implicit val timeWait: FiniteDuration = fd"2 second"
    val client = SchedulerClient.create("127.0.0.1", 5551, "127.0.0.1")
    val future = client
      .executeAll("test", Task(
        "test",
        3,
        "3s",
        1,
        TaskPropBuilder.python
          .pyFile(s"$path/test.py")
          .args("hello", "world")
          .waitForTimeout("100s")
          .handleStdout(LineHandler.fileOutputHandler(new File(s"$path/test1.txt")))
          .handleStderr(LineHandler.fileOutputHandler(new File(s"$path/test2.txt")))
          .build
      ))
    future
      .onComplete {
        case Success(value) =>
          value.handleState("2s", state => println("state is: " + state))
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
    startCluster(args)
    Thread.sleep(2000)
    startClient(args)
  }
}
