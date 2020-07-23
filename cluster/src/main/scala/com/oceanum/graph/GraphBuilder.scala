package com.oceanum.graph

import java.util.concurrent.atomic.AtomicInteger

import com.oceanum.graph.Operators.{Operator, Start}
import com.oceanum.graph.StreamFlows.{EndFlow, StartFlow}

import scala.collection.mutable

class GraphBuilder(val start: StartFlow,
                   val end: EndFlow,
                   implicit protected[graph] val dslBuilder: DslBuilder,
                   val handler: GraphMetaHandler) {
  val flowFactory: FlowFactory.type = FlowFactory
  val operatorFactory: OperatorFactory.type  = OperatorFactory

  private val taskIdValue: AtomicInteger = new AtomicInteger(0)
  protected[graph] def idValue: Int = taskIdValue.getAndIncrement()
  private val operators: mutable.Map[Int, Operator[_]] = mutable.Map()
  private val edges: mutable.Map[Int, Array[Int]] = mutable.Map()


  def add(from: Operator[_], to: Operator[_]*): GraphBuilder = {
    def getIdAndSet(operator: Operator[_]): Int = operators.find(_._2 == operator) match {
      case Some((i, _)) => i
      case None =>
        val id = idValue
        operators += (id -> operator)
        id
    }

    val fromId = getIdAndSet(from)
    val toIds = to.map(getIdAndSet).toArray
    edges += (fromId -> toIds)
    this
  }

  def build(): Unit = {
    def build(id: Int, operator: Operator[_]): Unit = {
      edges.get(id).foreach { arr =>
        arr.map(i => (i, operators(i)))
          .foreach(kv => {
            val (i, sub) = kv
            val subOperator = operator.connect(sub)
            operators += (i -> subOperator)
            build(i, subOperator)
          })
        edges - id
      }
    }
    operators.find(_._2.isInstanceOf[Start]) match {
      case Some((id, start)) => build(id, start)
      case _ => throw new IllegalArgumentException("需要start算子")
    }
  }
}

object GraphBuilder {
  def apply(start: StartFlow, end: EndFlow, dslBuilder: DslBuilder, handler: GraphMetaHandler): GraphBuilder = {
    new GraphBuilder(start, end, dslBuilder, handler)
  }
}