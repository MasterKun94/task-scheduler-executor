import java.util.Date

import com.oceanum.Test.Test
import com.oceanum.api.RemoteRestService
import com.oceanum.api.entities.{ConvergeVertex, Dag, DecisionVertex, EndVertex, ForkVertex, JoinVertex, StartVertex, TaskVertex, WorkflowDefine}
import com.oceanum.common.{Environment, FallbackStrategy, GraphContext, GraphMeta, ReRunStrategy, RichGraphMeta, RichTaskMeta, SystemInit}
import com.oceanum.persistence.Catalog
import com.oceanum.persistence.es.EsUtil
import com.oceanum.serialize.Serialization

import scala.util.{Failure, Success, Try}

/**
 * @author chenmingkun
 * @date 2020/7/25
 */
object Test0 {


  def main(args: Array[String]): Unit = {
    Environment.loadEnv(Array("--conf=cluster/src/main/resources/application.properties"))
    SystemInit.initAnnotatedClass()
//    val restService = SystemInit.restService
    val serialization = SystemInit.serialization
    val restService = new RemoteRestService("192.168.10.131")
    val name = "graph-define-test-4"
    val workflowDefine = WorkflowDefine(
      version = 1,
      name = name,
      Dag(
        vertexes = Map(
          "python1" -> TaskVertex(Test.task("1").copy(rawEnv = GraphContext(Map("file_name" -> "${(graph.id() % 2 == 0) ? 'python-err' : 'python'}")))),
          "python2" -> TaskVertex(Test.task("2")),
          "python3" -> TaskVertex(Test.task("3")),
          "python4" -> TaskVertex(Test.task("4")),
          "python5" -> TaskVertex(Test.task("5")),
          "fork" -> ForkVertex(2),
          "join" -> JoinVertex(2),
          "decision" -> DecisionVertex(Array("false")),
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
      )
      ,
      env = Map("file_name" -> "python")
    )

    def printResult[T<:AnyRef](value: Try[T]): Unit = value match {
      case Success(value) => println(serialization.serialize(value, pretty = true))
      case Failure(exception) => exception.printStackTrace()
    }
//    Catalog.save[GraphMeta]("test", new RichGraphMeta())
//    Thread.sleep(3000)

    import Environment.NONE_BLOCKING_EXECUTION_CONTEXT

//    restService.submitWorkflow(workflowDefine).onComplete(println)

//    restService.getWorkflow(workflowDefine.name).onComplete(printResult)
//
//    restService.runWorkflow(workflowDefine.name, FallbackStrategy.CONTINUE, version = None).onComplete(printResult)
//
//    restService.reRunWorkflow(workflowDefine.name, ReRunStrategy.RUN_ALL_AFTER_FAILED).onComplete(printResult)
//
    restService.checkWorkflowState(workflowDefine.name).onComplete(printResult)
  }
}
