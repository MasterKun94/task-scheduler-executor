package com.oceanum.utils

import java.net.{InetAddress, UnknownHostException}

import akka.util.Timeout
import com.oceanum.ClusterStarter
import com.oceanum.client.{SchedulerClient, Task}
import com.oceanum.common.Implicits._

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

/**
 * @author chenmingkun
 * @date 2020/6/18
 */
object Test {
//  val ip: String = getSelfAddress
//  val ip: String = "127.0.0.1"
  val ip2 = "192.168.10.131"
  val ip1 = "192.168.10.55"

  def task(id: String): Task = Task.builder.python()
    .id(id)
    .user("test1")
    .topic("default")
    .retryCount(3)
    .retryInterval("5 second")
    .priority(5)
    .pyFile("/tmp/test.py")
    .args("hello", "world")
    .waitForTimeout("100 second")
    .lazyInit(_
      .user("root")
    )
    .checkStateInterval("3s")
    .build

  def client: SchedulerClient = SchedulerClient(ip1, 5551, ip2, "src/main/resources/application.properties")

  def startCluster(args: Array[String]): Unit = {
    ClusterStarter.main(Array("--port=3551", "--topics=t1,a1", s"--host=$ip1", s"--seed-node=$ip1", "--conf=src"/"main"/"resources"/"application.properties,src"/"main"/"resources"/"application-env.properties"))
  }


  def startClient(args: Array[String]): Unit = {
//    val path = "C:"/"Users"/"chenmingkun"/"work"/"idea"/"work"/"task-scheduler-core"/"task-scheduler-executor"/"src"/"main"/"resources"
//    val path = "/root"/"test"

    implicit val executionContext: ExecutionContextExecutor = ExecutionContext.global
    val instanceRef = client
      .execute(task("test1"))
    instanceRef.completeState.onComplete("complete: " + _.get)
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
//    startCluster(args)
//    //    MetricsListener.start()
//    import com.oceanum.common.Implicits._
//    implicit val timeout: Timeout = fd"10 second"
////
//    scala.io.StdIn.readLine()
    val client = SchedulerClient(ip1, 5551, ip2, "src/main/resources/application.properties")
//    ClusterFileServer.start().value

//    scala.io.StdIn.readLine()
    println("handle cluster metrics")
    val hook = client.handleClusterMetrics("2s") { m =>
      m.nodeMetrics.foreach(println)
    }
    scala.io.StdIn.readLine()
    println("stop handle cluster metrics")
    hook.kill()
    scala.io.StdIn.readLine()
    println("handle cluster info")
    val hook1 = client.handleClusterInfo("2s")(println)
    scala.io.StdIn.readLine()
    println("stop handle cluster info")
    hook1.kill()
    scala.io.StdIn.readLine()
    println("handle task info")
    val hook2 = client.handleTaskInfo(println)
    scala.io.StdIn.readLine()
    startClient(args)
    scala.io.StdIn.readLine()
    println("stop handle task info")
    hook2.kill()

//    import ExecutionContext.Implicits.global
//    val task = Task.builder.python()
//      .id("test")
//      .topic("t1")
//      .retryCount(3)
//      .retryInterval("5 second")
//      .priority(5)
//      .pyFile("cluster://"/Environment.BASE_PATH/"src"/"main"/"resources"/"test.py")
//      .args("hello", "world")
//      .waitForTimeout("100s")
//      .build
//    println(task)
//      task
//      .init(null).onComplete(println)
  }
}
