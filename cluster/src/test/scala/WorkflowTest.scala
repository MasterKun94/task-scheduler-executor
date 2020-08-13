import java.util.Date

import com.oceanum.api.RemoteRestService
import com.oceanum.api.entities._
import com.oceanum.client.Task
import com.oceanum.common.Environment.NONE_BLOCKING_EXECUTION_CONTEXT
import com.oceanum.common._
import com.oceanum.persistence.Catalog

import scala.io.StdIn
import scala.util.{Failure, Success, Try}

/**
 * @author chenmingkun
 * @date 2020/7/25
 */
object WorkflowTest {
  val task: Task = Task.builder.python()
    .user("root")
    .name("task1")
    .topic("optimus10a131")
    .retryCount(3)
    .retryInterval("5 second")
    .priority(5)
    .pyFile("/tmp/task-test/${file_name}.py")
    .args("hello", "${task.id()}", "${task.startTime()}")
    .waitForTimeout("100 second")
    .checkStateInterval("3s")
    .parallelism(1)
    .build
  val task1: Task = task.copy(rawEnv = GraphContext(Map("file_name" -> "${(graph.rerunId() == 0) ? 'python-err' : 'python'}")))
  val task2: Task = task.copy(name = "task2")
  val task3: Task = task.copy(name = "task3")
  val task4: Task = task.copy(name = "task4")
  val task5: Task = task.copy(name = "task5")

  val workflowName = "graph-define-test-9"
  val workflowDefine: WorkflowDefine = WorkflowDefine(
    version = 1,
    name = workflowName,
    Dag(
      vertexes = Map(
        "python1" -> TaskVertex(task1),
        "python2" -> TaskVertex(task2),
        "python3" -> TaskVertex(task3),
        "python4" -> TaskVertex(task4),
        "python5" -> TaskVertex(task5),
        "fork" -> ForkVertex(2),
        "join" -> JoinVertex(2),
        "decision" -> DecisionVertex(Array("graph.id() % 2 == 1")),
        "converge" -> ConvergeVertex(2),
        "start" -> StartVertex(),
        "end" -> EndVertex()
      ),
      edges = Map(
        "start" -> Array("fork"),
        "fork" -> Array("python1", "python2"),
        "python1" -> Array("join"),
        "python2" -> Array("decision"),
        "decision" -> Array("python3", "python4"),
        "python3" -> Array("converge"),
        "python4" -> Array("converge"),
        "converge" -> Array("join"),
        "join" -> Array("python5"),
        "python5" -> Array("end")
      )
    ),
    env = Map("file_name" -> "python")
  )

  def main(args: Array[String]): Unit = {
    Environment.loadEnv(Array("--conf=cluster/src/main/resources/application.properties"))
    Environment.initSystem()
    val restService = new RemoteRestService("192.168.10.131")
//    Catalog.getRepository[GraphMeta].save("test", new RichGraphMeta())

    restService.submitWorkflow(workflowDefine).map(_ => BoolValue(true)).onComplete(printResult)
    StdIn.readLine()
    restService.getWorkflow(workflowDefine.name).onComplete(printResult)
    StdIn.readLine()
    restService.runWorkflow(workflowDefine.name, FallbackStrategy.CONTINUE, version = None, scheduleTime = Option(new Date())).onComplete(printResult)
    StdIn.readLine()
    restService.checkWorkflowStatus(workflowDefine.name).onComplete(printResult)
    StdIn.readLine()
    restService.rerunWorkflow(workflowDefine.name, RerunStrategy.RUN_ALL_AFTER_FAILED).onComplete(printResult)
    StdIn.readLine()
    restService.checkWorkflowStatus(workflowDefine.name).onComplete(printResult)
  }

  def printResult[T<:AnyRef](value: Try[T]): Unit = value match {
    case Success(value) => println(SystemInit.serialization.serialize(value, pretty = true))
    case Failure(exception) => exception.printStackTrace()
  }
}
