package com.oceanum.graph

import com.oceanum.client.{Task, TaskClient}
import com.oceanum.common.GraphMeta
import com.oceanum.graph.StreamFlows.{ConditionFlow, ConvergeFlow, DecisionFlow, EndFlow, ForkFlow, JoinFlow, OpsFlow, StartFlow, StreamFlow, TaskFlow}

object Operators {
  abstract class Operator[T<:StreamFlow](builder: GraphBuilder, taskClient: TaskClient) {
    lazy val flow: T = toFlow(builder, taskClient)

    protected def toFlow(implicit builder: GraphBuilder, taskClient: TaskClient): T = {
      throw new IllegalArgumentException()
    }

    def connect(operator: BaseOperator): BaseOperator = {
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

    override def connect(operator: BaseOperator): BaseOperator = operator match {
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

    override def connect(operator: BaseOperator): BaseOperator = operator match {
      case o: TaskOperator => Ops(flow --> o.flow)
      case o: Fork => Ops(flow --> o.flow)
      case o: Join => Ops(flow --> o.flow)
      case o: Decision => Ops(flow --> o.flow)
      case o: Converge => Ops(flow --> o.flow)
      case o: End => ReachEnd(flow --> o.flow)
    }
  }

  case class Decision(parallelism: Int, graphMeta: GraphMeta => Int)(implicit builder: GraphBuilder, taskClient: TaskClient)
    extends Operator[DecisionFlow](builder, taskClient)  {
    override protected def toFlow(implicit builder: GraphBuilder, taskClient: TaskClient): DecisionFlow = {
      FlowFactory.createDecision(parallelism)(graphMeta)
    }

    override def connect(operator: BaseOperator): BaseOperator = operator match {
      case o: TaskOperator => Ops(flow --> o.flow)
      case o: Fork => Ops(flow --> o.flow)
      case o: Join => Ops(flow --> o.flow)
      case o: Decision => Ops(flow --> o.flow)
      case o: Converge => Ops(flow --> o.flow)
    }
  }

  case class DecisionOut(decision: Decision, condition: Int)(implicit builder: GraphBuilder, taskClient: TaskClient)
    extends Operator[ConditionFlow](builder, taskClient)  {
    override protected def toFlow(implicit builder: GraphBuilder, taskClient: TaskClient): ConditionFlow = {
      decision.flow.condition(condition)
    }

    override def connect(operator: BaseOperator): BaseOperator = operator match {
      case o: TaskOperator => Ops(flow --> o.flow)
      case o: Fork => Ops(flow --> o.flow)
      case o: Join => Ops(flow --> o.flow)
      case o: Decision => Ops(flow --> o.flow)
      case o: Converge => Ops(flow --> o.flow)
    }
  }

  case class Converge(parallelism: Int)(implicit builder: GraphBuilder, taskClient: TaskClient)
    extends Operator[ConvergeFlow](builder, taskClient)  {
    override protected def toFlow(implicit builder: GraphBuilder, taskClient: TaskClient): ConvergeFlow = {
      FlowFactory.createConverge(parallelism)
    }

    override def connect(operator: BaseOperator): BaseOperator = operator match {
      case o: TaskOperator => Ops(flow --> o.flow)
      case o: Fork => Ops(flow --> o.flow)
      case o: Join => Ops(flow --> o.flow)
      case o: Decision => Ops(flow --> o.flow)
      case o: Converge => Ops(flow --> o.flow)
      case o: End => ReachEnd(flow --> o.flow)
    }
  }

  case class Start()(implicit builder: GraphBuilder, taskClient: TaskClient)
    extends Operator[StartFlow](builder, taskClient)  {
    override protected def toFlow(implicit builder: GraphBuilder, taskClient: TaskClient): StartFlow = {
      builder.startFlow
    }

    override def connect(operator: BaseOperator): BaseOperator = operator match {
      case o: TaskOperator => Ops(flow --> o.flow)
      case o: Fork => Ops(flow --> o.flow)
      case o: Decision => Ops(flow --> o.flow)
      case o: End => ReachEnd(flow --> o.flow)
    }
  }

  case class End()(implicit builder: GraphBuilder, taskClient: TaskClient)
    extends Operator[EndFlow](builder, taskClient)  {
    override protected def toFlow(implicit builder: GraphBuilder, taskClient: TaskClient): EndFlow = {
      builder.endFlow
    }
  }

  case class Ops(opsFlow: OpsFlow)(implicit builder: GraphBuilder, taskClient: TaskClient)
    extends Operator[OpsFlow](builder, taskClient)  {
    override protected def toFlow(implicit builder: GraphBuilder, taskClient: TaskClient): OpsFlow = opsFlow

    override def connect(operator: BaseOperator): BaseOperator = operator match {
      case o: TaskOperator => Ops(flow --> o.flow)
      case o: Fork => Ops(flow --> o.flow)
      case o: Join => Ops(flow --> o.flow)
      case o: Decision => Ops(flow --> o.flow)
      case o: Converge => Ops(flow --> o.flow)
      case o: End => ReachEnd(flow --> o.flow)
    }
  }

  case class ReachEnd(unit: Unit)(implicit builder: GraphBuilder, taskClient: TaskClient)
    extends Operator[Nothing](builder, taskClient)  {
  }
}
