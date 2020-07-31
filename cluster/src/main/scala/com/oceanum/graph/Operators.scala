package com.oceanum.graph

import com.oceanum.annotation.ISerializationMessage
import com.oceanum.client.{Task, TaskClient}
import com.oceanum.graph.StreamFlows._

object Operators {
  abstract class Operator[T<:StreamFlow] {
    private var builder: GraphBuilder = _
    private var taskClient: TaskClient = _
    private lazy val inner: T = createFlow(builder, taskClient)
    protected[graph] def flow(implicit builder: GraphBuilder, taskClient: TaskClient): T = {
      this.builder = builder
      this.taskClient = taskClient
      inner
    }

    protected def createFlow(implicit builder: GraphBuilder, taskClient: TaskClient): T = {
      throw new IllegalArgumentException()
    }

    protected[graph] def connect(operator: BaseOperator)(implicit builder: GraphBuilder, taskClient: TaskClient): BaseOperator = {
      throw new IllegalArgumentException("不支持" + this.getClass + "和" + operator.getClass + "的关联")
    }
  }

  @ISerializationMessage("TASK_OPERATOR")
  case class TaskOperator(task: Task, parallelism: Int = 1)
    extends Operator[TaskFlow] {
    override protected def createFlow(implicit builder: GraphBuilder, taskClient: TaskClient): TaskFlow = {
      FlowFactory.createFlow(task, parallelism)
    }
  }

  @ISerializationMessage("FORK_OPERATOR")
  case class Fork(parallelism: Int)
    extends Operator[ForkFlow]  {
    override protected def createFlow(implicit builder: GraphBuilder, taskClient: TaskClient): ForkFlow = {
      FlowFactory.createFork(parallelism)
    }

    override protected[graph] def connect(operator: BaseOperator)(implicit builder: GraphBuilder, taskClient: TaskClient): BaseOperator = operator match {
        case o: TaskOperator => Ops(flow --> o.flow)
        case o: Fork => Ops(flow --> o.flow)
        case o: Join => Ops(flow --> o.flow)
        case o: Decision => Ops(flow --> o.flow)
        case o: Converge => Ops(flow --> o.flow)
    }
  }

  @ISerializationMessage("JOIN_OPERATOR")
  case class Join(parallelism: Int)
    extends Operator[JoinFlow]  {
    override protected def createFlow(implicit builder: GraphBuilder, taskClient: TaskClient): JoinFlow = {
      FlowFactory.createJoin(parallelism)
    }

    override protected[graph] def connect(operator: BaseOperator)(implicit builder: GraphBuilder, taskClient: TaskClient): BaseOperator = operator match {
      case o: TaskOperator => Ops(flow --> o.flow)
      case o: Fork => Ops(flow --> o.flow)
      case o: Join => Ops(flow --> o.flow)
      case o: Decision => Ops(flow --> o.flow)
      case o: Converge => Ops(flow --> o.flow)
      case o: End => ReachEnd(flow --> o.flow)
    }
  }

  @ISerializationMessage("DECISION_OPERATOR")
  case class Decision(expr: Array[String])
    extends Operator[DecisionFlow]  {
    override protected def createFlow(implicit builder: GraphBuilder, taskClient: TaskClient): DecisionFlow = {
      FlowFactory.createDecision(expr)
    }

    override protected[graph] def connect(operator: BaseOperator)(implicit builder: GraphBuilder, taskClient: TaskClient): BaseOperator = operator match {
      case o: TaskOperator => Ops(flow --> o.flow)
      case o: Fork => Ops(flow --> o.flow)
      case o: Join => Ops(flow --> o.flow)
      case o: Decision => Ops(flow --> o.flow)
      case o: Converge => Ops(flow --> o.flow)
    }
  }

  @ISerializationMessage("DECISION_OUT_OPERATOR")
  case class DecisionOut(decision: Decision, condition: Int)
    extends Operator[ConditionFlow]  {
    override protected def createFlow(implicit builder: GraphBuilder, taskClient: TaskClient): ConditionFlow = {
      decision.flow.condition(condition)
    }

    override protected[graph] def connect(operator: BaseOperator)(implicit builder: GraphBuilder, taskClient: TaskClient): BaseOperator = operator match {
      case o: TaskOperator => Ops(flow --> o.flow)
      case o: Fork => Ops(flow --> o.flow)
      case o: Join => Ops(flow --> o.flow)
      case o: Decision => Ops(flow --> o.flow)
      case o: Converge => Ops(flow --> o.flow)
    }
  }

  @ISerializationMessage("CONVERGE_OPERATOR")
  case class Converge(parallelism: Int)
    extends Operator[ConvergeFlow]  {
    override protected def createFlow(implicit builder: GraphBuilder, taskClient: TaskClient): ConvergeFlow = {
      FlowFactory.createConverge(parallelism)
    }

    override protected[graph] def connect(operator: BaseOperator)(implicit builder: GraphBuilder, taskClient: TaskClient): BaseOperator = operator match {
      case o: TaskOperator => Ops(flow --> o.flow)
      case o: Fork => Ops(flow --> o.flow)
      case o: Join => Ops(flow --> o.flow)
      case o: Decision => Ops(flow --> o.flow)
      case o: Converge => Ops(flow --> o.flow)
      case o: End => ReachEnd(flow --> o.flow)
    }
  }

  @ISerializationMessage("START_OPERATOR")
  case class Start()
    extends Operator[StartFlow]  {
    override protected def createFlow(implicit builder: GraphBuilder, taskClient: TaskClient): StartFlow = {
      builder.startFlow
    }

    override protected[graph] def connect(operator: BaseOperator)(implicit builder: GraphBuilder, taskClient: TaskClient): BaseOperator = operator match {
      case o: TaskOperator => Ops(flow --> o.flow)
      case o: Fork => Ops(flow --> o.flow)
      case o: Decision => Ops(flow --> o.flow)
      case o: End => ReachEnd(flow --> o.flow)
    }
  }

  @ISerializationMessage("END_OPERATOR")
  case class End()
    extends Operator[EndFlow]  {
    override protected def createFlow(implicit builder: GraphBuilder, taskClient: TaskClient): EndFlow = {
      builder.endFlow
    }
  }

  case class Ops(opsFlow: OpsFlow)
    extends Operator[OpsFlow]  {
    override protected def createFlow(implicit builder: GraphBuilder, taskClient: TaskClient): OpsFlow = opsFlow

    override protected[graph] def connect(operator: BaseOperator)(implicit builder: GraphBuilder, taskClient: TaskClient): BaseOperator = operator match {
      case o: TaskOperator => Ops(flow --> o.flow)
      case o: Fork => Ops(flow --> o.flow)
      case o: Join => Ops(flow --> o.flow)
      case o: Decision => Ops(flow --> o.flow)
      case o: Converge => Ops(flow --> o.flow)
      case o: End => ReachEnd(flow --> o.flow)
    }
  }

  case class ReachEnd(unit: Unit)
    extends Operator[Nothing]  {
  }
}
