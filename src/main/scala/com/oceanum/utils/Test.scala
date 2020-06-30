package com.oceanum.utils

import java.net.{InetAddress, UnknownHostException}

import akka.util.Timeout
import com.oceanum.ClusterStarter
import com.oceanum.client.{SchedulerClient, Task, TaskPropBuilder}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.util.{Failure, Success}

/**
 * @author chenmingkun
 * @date 2020/6/18
 */
object Test {
  val ip: String = getSelfAddress

  def startCluster(args: Array[String]): Unit = {
    ClusterStarter.main(Array("--port=3551", "--topics=t1,a1", s"--host=$ip", s"--seed-node=$ip", "--conf=src\\main\\resources\\application.properties,src\\main\\resources\\application-env.properties"))
  }


  def startClient(args: Array[String]): Unit = {
    import com.oceanum.client.Implicits._
//    val path = "C:\\Users\\chenmingkun\\work\\idea\\work\\task-scheduler-core\\task-scheduler-executor\\src\\main\\resources"
    val path = "E:\\chenmingkun\\task-scheduler-executor\\src\\main\\resources"
//    val path = "/root/test"
    implicit val executionContext: ExecutionContextExecutor = ExecutionContext.global
    implicit val timeout: Timeout = fd"10 second"
    val client = SchedulerClient(ip, 5551, ip)
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
//          Thread.sleep(7000)
//          value.kill()
        case Failure(exception) =>
          exception.printStackTrace()
      }
    Thread.sleep(100000)
    SchedulerClient.terminate()
  }

  def getSelfAddress: String = {
//    "127.0.0.1"
    try {
      InetAddress.getLocalHost.getHostAddress
    } catch {
      case _: UnknownHostException => "127.0.0.1"
      case e: Throwable => throw e
    }
  }

  def main(args: Array[String]): Unit = {
    startCluster(args)
//    //    MetricsListener.start()
//    import com.oceanum.client.Implicits._
//    implicit val timeout: Timeout = fd"10 second"
//
//    scala.io.StdIn.readLine()
//    val client = SchedulerClient(ip, 5555, ip)
//    scala.io.StdIn.readLine()
//    println("handle cluster metrics")
//    val hook = client.handleClusterMetrics("2s") { m =>
//      m.nodeMetrics.foreach(println)
//    }
//    scala.io.StdIn.readLine()
//    println("stop handle cluster metrics")
//    hook.kill()
//    scala.io.StdIn.readLine()
//    println("handle cluster info")
//    val hook1 = client.handleClusterInfo("2s")(println)
//    scala.io.StdIn.readLine()
//    println("stop handle cluster info")
//    hook1.kill()
    scala.io.StdIn.readLine()
    startClient(args)
  }
}
