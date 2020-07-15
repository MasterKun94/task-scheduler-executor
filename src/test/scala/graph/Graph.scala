package graph

import com.oceanum.client.SchedulerClient
import com.oceanum.graph.RichGraphMeta
import com.oceanum.utils.Test

object Graph extends App {
  implicit val client: SchedulerClient = Test.client
  import com.oceanum.graph.FlowFactory._

  val (actor, future) = createGraph { implicit graph =>

    val task1 = createFlow(Test.task("task1"))
    val task2 = createFlow(Test.task("task2"))
    val task3 = createFlow(Test.task("task3"))
    val task4 = createFlow(Test.task("task4"))
    val task5 = createFlow(Test.task("task5"))
    val fork = createFork(2)
    val join = createJoin(2)
    val decision = createDecision(2)(_ => 1)
    val converge = createConverge(2)

    graph.start --> fork
    fork --> task1 --> join
    fork --> task2 --> decision
    decision --> task3 --> converge
    decision --> task4 --> converge
    converge --> task5 --> join
    join --> graph.end

  }.run()

  actor ! RichGraphMeta()
}