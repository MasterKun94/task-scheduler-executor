import java.net.{InetAddress, UnknownHostException}
import java.util.Date

import com.oceanum.client.{PluggableTaskProp, Task, TaskClient}
import com.oceanum.common.RichGraphMeta

import scala.util.{Failure, Success}

/**
 * @author chenmingkun
 * @date 2020/6/18
 */
object TaskTest {

  def task(name: String = "test"): Task = Task.builder.python()
    .user("root")
    .name(name)
    .topic("optimus10a131")
    .retryCount(3)
    .retryInterval("5 second")
    .priority(5)
    .pyFile("hdfs:///tmp/task-test/python.py")
    .args("hello")
    .waitForTimeout("100 second")
    .checkStateInterval("3s")
    .parallelism(1)
    .build
    .addGraphMeta(new RichGraphMeta().copy(id = 0, scheduleTime = Some(new Date())))
//    .copy(prop = new PluggableTaskProp(plugClass = "com.oceanum.pluggable.DemoExecutor", jars = Array.empty))

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
    import com.oceanum.common.Environment.NONE_BLOCKING_EXECUTION_CONTEXT
    val client = TaskClient(getSelfAddress, 5551, "192.168.10.131", "cluster/src/main/resources/application.properties")
    val instance = client.execute(task())
    instance.completeState.onComplete {
      case Success(value) =>
        println(value)
        value.metadata.error.foreach(_.printStackTrace())
      case Failure(exception) =>
        exception.printStackTrace()
    }
    Thread.sleep(5000)
    instance.kill().onComplete(println)
  }
}
