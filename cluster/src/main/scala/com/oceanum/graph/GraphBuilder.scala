package com.oceanum.graph

import java.util.concurrent.atomic.AtomicInteger

import com.oceanum.client.{Task, TaskClient}
import com.oceanum.common.GraphMeta
import com.oceanum.graph.Operators.{Converge, Decision, End, Fork, Join, Operator, Start, TaskOperator}
import com.oceanum.graph.StreamFlows.{ConvergeFlow, DecisionFlow, EndFlow, ForkFlow, JoinFlow, StartFlow, StreamFlow, TaskFlow}

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

  implicit val flowFactory: FlowFactory.type = FlowFactory

  def createStart(implicit taskClient: TaskClient): Start = OperatorFactory.createStart

  def createEnd(implicit taskClient: TaskClient): End = OperatorFactory.createEnd

  def createFlow(task: Task, parallelism: Int = 1)(implicit taskClient: TaskClient): TaskOperator = OperatorFactory.createFlow(task, parallelism)

  def createFork(parallel: Int)(implicit taskClient: TaskClient): Fork = OperatorFactory.createFork(parallel)

  def createJoin(parallel: Int)(implicit taskClient: TaskClient): Join = OperatorFactory.createJoin(parallel)

  def createDecision(parallel: Int)(decide: GraphMeta => Int)(implicit taskClient: TaskClient): Decision = OperatorFactory.createDecision(parallel)(decide)

  def createDecision(decide: Array[GraphMeta => Boolean])(implicit taskClient: TaskClient): Decision = OperatorFactory.createDecision(decide)

  def createDecision(expr: Array[String])(implicit taskClient: TaskClient): Decision = OperatorFactory.createDecision(expr)

  def createConverge(parallel: Int)(implicit taskClient: TaskClient): Converge = OperatorFactory.createConverge(parallel)

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

  def createGraph(): Unit = {
    def build(id: Int, operator: BaseOperator): Unit = {
      edges.get(id).foreach { node =>
        val subArr = node
          .map(i => {
            println(id + " " + operator + "\tconnect\t" + i + " " + operators(i))
            val to = operators(i)
            val subOperator: BaseOperator = operator.connect(to)
            //            if (!to.isInstanceOf[Fork] && !to.isInstanceOf[Decision]) {
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
    println(operators.mkString("\n"))
    println(edges.mapValues(_.toSeq).mkString("\n"))
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