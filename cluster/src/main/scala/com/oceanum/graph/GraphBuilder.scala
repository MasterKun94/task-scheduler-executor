package com.oceanum.graph

import java.util.concurrent.atomic.AtomicInteger

import com.oceanum.client.{Task, TaskClient}
import com.oceanum.graph.Operators._
import com.oceanum.graph.StreamFlows.{EndFlow, StartFlow, StreamFlow}

import scala.collection.mutable

class GraphBuilder(protected[graph] val startFlow: StartFlow,
                   protected[graph] val endFlow: EndFlow,
                   protected[graph] val handler: GraphMetaHandler,
                   implicit protected[graph] val dslBuilder: DslBuilder) {

  private val taskIdValue: AtomicInteger = new AtomicInteger(0)
  protected[graph] def idValue: Int = taskIdValue.getAndIncrement()
  private val operators: mutable.Map[Int, Operator[_<:StreamFlow]] = mutable.Map()
  private val edges: mutable.Map[Int, Array[Int]] = mutable.Map()
  private implicit val builder: GraphBuilder = this
  private val env: mutable.Map[String, Any] = mutable.Map.empty

  implicit val flowFactory: FlowFactory.type = FlowFactory

  def addEnv(kv: (String, Any)): GraphBuilder = {
    env += kv
    this
  }

  def addEnv(map: Map[String, Any]): GraphBuilder = {
    env ++= map
    this
  }

  def getEnv: Map[String, Any] = Map() ++ env

  def createStart(implicit taskClient: TaskClient): Start = Start()

  def createEnd(implicit taskClient: TaskClient): End = End()

  def createFlow(task: Task, parallelism: Int = 1)(implicit taskClient: TaskClient): TaskOperator = TaskOperator(task, parallelism)

  def createFork(parallel: Int)(implicit taskClient: TaskClient): Fork = Fork(parallel)

  def createJoin(parallel: Int)(implicit taskClient: TaskClient): Join = Join(parallel)

  def createDecision(expr: Array[String])(implicit taskClient: TaskClient): Decision = Decision(expr)

  def createConverge(parallel: Int)(implicit taskClient: TaskClient): Converge = Converge(parallel)

  def buildEdges(from: BaseOperator, to: BaseOperator*): GraphBuilder = {
    def getIdAndSet(operator: BaseOperator): Int = operators.find(_._2 == operator) match {
      case Some((i, _)) => i
      case None =>
        val id = idValue
        operators += (id -> operator)
        id
    }

    val fromId = getIdAndSet(from)
    val toIds = to.map(getIdAndSet).toArray
    edges += (fromId -> (edges.getOrElse(fromId, Array.empty) ++ toIds))
    this
  }

  def createGraph()(implicit builder: GraphBuilder, taskClient: TaskClient): Unit = {
    def build(id: Int, operator: BaseOperator)(implicit builder: GraphBuilder, taskClient: TaskClient): Unit = {
      edges.get(id).foreach { node =>
        val subArr = node
          .map(i => {
            val to = operators(i)
            val subOperator: BaseOperator = operator.connect(to)
            if (to.isInstanceOf[TaskOperator]) {
              operators += (i -> subOperator)
              (i, subOperator)
            } else {
              (i, to)
            }

          })
        edges -= id
        for ((subId, subOperator) <- subArr) {
          build(subId, subOperator)
        }
      }
    }
    taskIdValue.set(0)
    operators.find(_._2.isInstanceOf[Start]) match {
      case Some((id, start)) => build(id, start)
      case _ => throw new IllegalArgumentException("需要start算子")
    }
  }
}

object GraphBuilder {
  def apply(start: StartFlow, end: EndFlow, handler: GraphMetaHandler, dslBuilder: DslBuilder): GraphBuilder = {
    new GraphBuilder(start, end, handler, dslBuilder)
  }
}