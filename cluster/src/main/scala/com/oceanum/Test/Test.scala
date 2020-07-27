package com.oceanum.Test

import java.net.{InetAddress, UnknownHostException}

import com.oceanum.ClusterStarter
import com.oceanum.client.{TaskClient, Task}
import com.oceanum.common.Implicits._

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

/**
 * @author chenmingkun
 * @date 2020/6/18
 */
object Test {
  val ip2: String = getSelfAddress
//  val ip: String = "127.0.0.1"
//  val ip2 = "192.168.10.131"
  val ip1 = getSelfAddress

//  def task(): Task = Task.builder.python()
//    .user("test1")
//    .topic("default")
//    .retryCount(3)
//    .retryInterval("5 second")
//    .priority(5)
//    .pyFile("/tmp/task-test/${file_name}.py")
//    .args("hello")
//    .waitForTimeout("100 second")
//    .lazyInit(_
//      .user("root")
//    )
//    .checkStateInterval("3s")
//    .parallelism(1)
//    .build

  def task(name: String = "name"): Task = Task.builder.python()
    .user("root")
    .name(name)
    .topic("default")
    .retryCount(3)
    .retryInterval("5 second")
    .priority(5)
    .pyFile("/tmp/task-test/${file_name}.py")
    .args("hello", "${task.id()}", "${task.startTime()}", "${option(task.name(graph.findTask('2')), 'null')}")
    .waitForTimeout("100 second")
    .checkStateInterval("3s")
    .parallelism(1)
    .build

  def client: TaskClient = TaskClient(ip1, 5555, ip2, "src/main/resources/application.properties")

  def startCluster(args: Array[String]): Unit = {
    ClusterStarter.main(Array(s"--host=$ip1", s"--seed-node=$ip1", "--conf=cluster"/"src"/"main"/"resources"/"application.properties"))
  }


  def startClient(args: Array[String]): Unit = {
//    val path = "C:"/"Users"/"chenmingkun"/"work"/"idea"/"work"/"task-scheduler-core"/"task-scheduler-executor"/"src"/"main"/"resources"
//    val path = "/root"/"test"

    implicit val executionContext: ExecutionContextExecutor = ExecutionContext.global
    val instanceRef = client
      .execute(task())
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
    startCluster(args)
////    //    MetricsListener.start()
////    import com.oceanum.common.Implicits._
////    implicit val timeout: Timeout = fd"10 second"
//////
////    scala.io.StdIn.readLine()
//    val client = TaskClient(ip1, 5551, ip2, "src/main/resources/application.properties")
////    ClusterFileServer.start().value
//
////    scala.io.StdIn.readLine()
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
//    scala.io.StdIn.readLine()
//    println("handle task info")
//    val hook2 = client.handleTaskInfo(println)
//    scala.io.StdIn.readLine()
//    startClient(args)
//    scala.io.StdIn.readLine()
//    println("stop handle task info")
//    hook2.kill()
//
////    import ExecutionContext.Implicits.global
////    val task = Task.builder.python()
////      .id("test")
////      .topic("t1")
////      .retryCount(3)
////      .retryInterval("5 second")
////      .priority(5)
////      .pyFile("cluster://"/Environment.BASE_PATH/"src"/"main"/"resources"/"python-err.py")
////      .args("hello", "world")
////      .waitForTimeout("100s")
////      .build
////    println(task)
////      task
////      .init(null).onComplete(println)
  }
}
