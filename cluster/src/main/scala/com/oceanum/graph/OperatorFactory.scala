package com.oceanum.graph

import com.oceanum.client.{Task, TaskClient}
import com.oceanum.common.{ExprContext, GraphMeta, RichGraphMeta}
import com.oceanum.expr.Evaluator
import com.oceanum.graph.Operators.{Converge, Decision, End, Fork, Join, Start, TaskOperator}

object OperatorFactory {
  def createFlow(task: Task, parallelism: Int = 1)(implicit taskClient: TaskClient, builder: GraphBuilder): TaskOperator = {
    TaskOperator(task, parallelism)
  }

  def createStart(implicit taskClient: TaskClient, builder: GraphBuilder): Start = Start()

  def createEnd(implicit taskClient: TaskClient, builder: GraphBuilder): End = End()

  def createFork(parallel: Int)(implicit taskClient: TaskClient, builder: GraphBuilder): Fork = {
    Fork(parallel)
  }

  def createJoin(parallel: Int)(implicit taskClient: TaskClient, builder: GraphBuilder): Join = {
    Join(parallel)
  }

  def createDecision(parallel: Int)(decide: GraphMeta => Int)(implicit taskClient: TaskClient, builder: GraphBuilder): Decision = {
    Decision(parallel, decide)
  }

  def createDecision(decide: Array[GraphMeta => Boolean])(implicit taskClient: TaskClient, builder: GraphBuilder): Decision = {
    val parallel = decide.length + 1
    val func: GraphMeta => Int = meta => decide.zipWithIndex.find(_._1(meta)).map(_._2) match {
      case Some(int) => int
      case None => parallel - 1
    }
    createDecision(parallel)(func)
  }

  def createDecision(expr: Array[String])(implicit taskClient: TaskClient, builder: GraphBuilder): Decision = {
    val meta2env = (meta: GraphMeta) => (ExprContext(meta.env) + meta.asInstanceOf[RichGraphMeta]).toJava
    val decide = expr
      .map(Evaluator.compile(_, cache = false))
      .map(expr => (meta: GraphMeta) => expr.execute(meta2env(meta)).asInstanceOf[Boolean])
    createDecision(decide)
  }

  def createConverge(parallel: Int)(implicit builder: GraphBuilder, taskClient: TaskClient): Converge = {
    Converge(parallel)
  }
}
