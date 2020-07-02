package com.oceanum.utils

import java.net.{InetAddress, UnknownHostException}

import akka.util.Timeout
import com.oceanum.ClusterStarter
import com.oceanum.client.{SchedulerClient, Task, TaskPropBuilder}
import com.oceanum.common.Environment

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.util.{Failure, Success}
import com.oceanum.common.Implicits._

/**
 * @author chenmingkun
 * @date 2020/6/18
 */
object Test {
//  val ip: String = getSelfAddress
  val ip: String = "127.0.0.1"

  def startCluster(args: Array[String]): Unit = {
    ClusterStarter.main(Array("--port=3551", "--topics=t1,a1", s"--host=$ip", s"--seed-node=$ip", "--conf=src"/"main"/"resources"/"application.properties,src"/"main"/"resources"/"application-env.properties"))
  }


  def startClient(args: Array[String]): Unit = {
//    val path = "C:"/"Users"/"chenmingkun"/"work"/"idea"/"work"/"task-scheduler-core"/"task-scheduler-executor"/"src"/"main"/"resources"
//    val path = "/root"/"test"

    implicit val executionContext: ExecutionContextExecutor = ExecutionContext.global
    val client = SchedulerClient(ip, 5551, ip)
    val future = client
      .executeAll(Task.builder.python()
          .id("test")
          .topic("t1")
          .retryCount(3)
          .retryInterval("5 second")
          .priority(5)
          .pyFile(Environment.BASE_PATH/"src"/"main"/"resources"/"test.py")
          .args("hello", "world")
          .waitForTimeout("100 second")
          .build)
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
    client.close
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
    //    MetricsListener.start()
    import com.oceanum.common.Implicits._
    implicit val timeout: Timeout = fd"10 second"
//
    scala.io.StdIn.readLine()
    val client = SchedulerClient(ip, 5555, ip)
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
    println("handle task info")
    val hook2 = client.handleTaskInfo(println)
    scala.io.StdIn.readLine()
    println("stop handle task info")
//    hook2.kill()
    scala.io.StdIn.readLine()
    startClient(args)
  }
}
