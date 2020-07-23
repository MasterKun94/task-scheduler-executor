package com.oceanum.graph

import com.oceanum.client.{Task, TaskClient}
import com.oceanum.common.GraphMeta
import com.oceanum.graph.StreamFlows.{ConditionFlow, ConvergeFlow, DecisionFlow, EndFlow, ForkFlow, JoinFlow, OpsFlow, StartFlow, StreamFlow, TaskFlow}

object Operators {
  abstract class Operator[T<:StreamFlow](builder: GraphBuilder, taskClient: TaskClient) {
    lazy val flow: T = toFlow(builder, taskClient)

    protected def toFlow(implicit builder: GraphBuilder, taskClient: TaskClient): T

    def connect(operator: Operator[_]): Operator[_] = {
      throw new IllegalArgumentException("不支持" + this.getClass + "和" + operator.getClass + "的关联")
    }
  }

  case class TaskOperator(task: Task, parallelism: Int = 1)(implicit builder: GraphBuilder, taskClient: TaskClient)
    extends Operator[TaskFlow](builder, taskClient) {
    override protected def toFlow(implicit builder: GraphBuilder, taskClient: TaskClient): TaskFlow = {
      FlowFactory.createFlow(task, parallelism)
    }
  }

  case class Fork(parallelism: Int)(implicit builder: GraphBuilder, taskClient: TaskClient)
    extends Operator[ForkFlow](builder, taskClient)  {
    override protected def toFlow(implicit builder: GraphBuilder, taskClient: TaskClient): ForkFlow = {
      FlowFactory.createFork(parallelism)
    }

    override def connect(operator: Operator[_]): Operator[_] = operator match {
        case o: TaskOperator => Ops(flow --> o.flow)
        case o: Fork => Ops(flow --> o.flow)
        case o: Join => Ops(flow --> o.flow)
        case o: Decision => Ops(flow --> o.flow)
        case o: Converge => Ops(flow --> o.flow)
    }
  }

  case class Join(parallelism: Int)(implicit builder: GraphBuilder, taskClient: TaskClient)
    extends Operator[JoinFlow](builder, taskClient)  {
    override protected def toFlow(implicit builder: GraphBuilder, taskClient: TaskClient): JoinFlow = {
      FlowFactory.createJoin(parallelism)
    }

    override def connect(operator: Operator[_]): Operator[_] = ???
  }

  case class Decision(parallelism: Int, graphMeta: GraphMeta => Int)(implicit builder: GraphBuilder, taskClient: TaskClient)
    extends Operator[DecisionFlow](builder, taskClient)  {
    override protected def toFlow(implicit builder: GraphBuilder, taskClient: TaskClient): DecisionFlow = {
      FlowFactory.createDecision(parallelism)(graphMeta)
    }

    override def connect(operator: Operator[_]): Operator[_] = ???
  }

  case class DecisionOut(decision: Decision, condition: Int)(implicit builder: GraphBuilder, taskClient: TaskClient)
    extends Operator[ConditionFlow](builder, taskClient)  {
    override protected def toFlow(implicit builder: GraphBuilder, taskClient: TaskClient): ConditionFlow = {
      decision.flow.condition(condition)
    }

    override def connect(operator: Operator[_]): Operator[_] = ???
  }

  case class Converge(parallelism: Int)(implicit builder: GraphBuilder, taskClient: TaskClient)
    extends Operator[ConvergeFlow](builder, taskClient)  {
    override protected def toFlow(implicit builder: GraphBuilder, taskClient: TaskClient): ConvergeFlow = {
      FlowFactory.createConverge(parallelism)
    }

    override def connect(operator: Operator[_]): Operator[_] = ???
  }

  case class Start()(implicit builder: GraphBuilder, taskClient: TaskClient)
    extends Operator[StartFlow](builder, taskClient)  {
    override protected def toFlow(implicit builder: GraphBuilder, taskClient: TaskClient): StartFlow = {
      builder.start
    }

    override def connect(operator: Operator[_]): Operator[_] = ???
  }

  case class End()(implicit builder: GraphBuilder, taskClient: TaskClient)
    extends Operator[EndFlow](builder, taskClient)  {
    override protected def toFlow(implicit builder: GraphBuilder, taskClient: TaskClient): EndFlow = {
      builder.end
    }
  }

  case class Ops(opsFlow: OpsFlow)(implicit builder: GraphBuilder, taskClient: TaskClient)
    extends Operator[OpsFlow](builder, taskClient)  {
    override protected def toFlow(implicit builder: GraphBuilder, taskClient: TaskClient): OpsFlow = opsFlow

    override def connect(operator: Operator[_]): Operator[_] = ???
  }
}
