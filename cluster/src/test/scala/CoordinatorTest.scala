import java.util.Date

import com.oceanum.api.RemoteRestService
import com.oceanum.api.entities.{Coordinator, TriggerConfig, WorkflowDefine}
import com.oceanum.client.Task
import com.oceanum.common.{Environment, FallbackStrategy}
import WorkflowTest.printResult
import Environment.NONE_BLOCKING_EXECUTION_CONTEXT

import scala.io.StdIn

object CoordinatorTest {
  val task1: Task = WorkflowTest.task1
  val task2: Task = WorkflowTest.task2
  val task3: Task = WorkflowTest.task3
  val task4: Task = WorkflowTest.task4
  val task5: Task = WorkflowTest.task5
  val wfDefine: WorkflowDefine = WorkflowTest.workflowDefine
  val coordinatorName = "coordinator-test"
  val coordinator: Coordinator = Coordinator(
    name = coordinatorName,
    fallbackStrategy = FallbackStrategy.CONTINUE,
    trigger = TriggerConfig(
      name = "QUARTZ",
      config = Map(
        "cron" -> "0 * * * * ? *"
      )
    ),
    workflowDefine = wfDefine,
    version = 0,
    startTime = Some(new Date(System.currentTimeMillis() + 3 * 60 * 1000L))
  )

  def main(args: Array[String]): Unit = {
    Environment.loadEnv(Array("--conf=cluster/src/main/resources/application.properties"))
    Environment.initSystem()
    val restService = new RemoteRestService("192.168.10.131")
    restService.submitCoordinator(coordinator).onComplete(println)
    StdIn.readLine()
    restService.getCoordinator(coordinator.name).onComplete(printResult)
    StdIn.readLine()
    restService.runCoordinator(coordinator.name).onComplete(println)
    StdIn.readLine()
    restService.checkCoordinatorStatus(coordinator.name).onComplete(printResult)
    StdIn.readLine()
    restService.suspendCoordinator(coordinator.name).onComplete(println)
    StdIn.readLine()
    restService.checkCoordinatorStatus(coordinator.name).onComplete(printResult)
    StdIn.readLine()
    restService.resumeCoordinator(coordinator.name).onComplete(println)
    StdIn.readLine()
    restService.checkCoordinatorStatus(coordinator.name).onComplete(printResult)
    StdIn.readLine()
    restService.stopCoordinator(coordinator.name).onComplete(println)
    StdIn.readLine()
    restService.checkCoordinatorStatus(coordinator.name).onComplete(printResult)
  }

}
