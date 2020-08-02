package com.oceanum.graph

import akka.NotUsed
import akka.actor.ActorRef
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{Flow, Source}
import akka.stream.{Outlet, SinkShape, UniformFanInShape, UniformFanOutShape}
import com.oceanum.common.RichGraphMeta

object StreamFlows {

  trait StreamFlow {}

  case class TaskFlow(elem: Flow[Message, Message, NotUsed], id: Int) extends StreamFlow

  case class ForkFlow(elem: UniformFanOutShape[Message, Message]) extends StreamFlow {
    def -->(flow: TaskFlow)(implicit builder: GraphBuilder): OpsFlow = {
      import builder.dslBuilder
      OpsFlow(elem ~> flow.elem)
    }
    def -->(flow: ForkFlow)(implicit builder: GraphBuilder): OpsFlow = {
      import builder.dslBuilder
      OpsFlow(elem ~> flow.elem)
    }
    def -->(flow: JoinFlow)(implicit builder: GraphBuilder): OpsFlow = {
      import builder.dslBuilder
      OpsFlow(elem ~> flow.elem)
    }
    def -->(flow: DecisionFlow)(implicit builder: GraphBuilder): OpsFlow = {
      import builder.dslBuilder
      OpsFlow(elem ~> flow.elem)
    }
    def -->(flow: ConvergeFlow)(implicit builder: GraphBuilder): OpsFlow = {
      import builder.dslBuilder
      OpsFlow(elem ~> flow.elem)
    }
  }

  case class JoinFlow(elem: UniformFanInShape[Message, Message]) extends StreamFlow {
    def -->(flow: TaskFlow)(implicit builder: GraphBuilder): OpsFlow = {
      import builder.dslBuilder
      OpsFlow(elem ~> flow.elem)
    }
    def -->(flow: ForkFlow)(implicit builder: GraphBuilder): OpsFlow = {
      import builder.dslBuilder
      OpsFlow(elem ~> flow.elem)
    }
    def -->(flow: JoinFlow)(implicit builder: GraphBuilder): OpsFlow = {
      import builder.dslBuilder
      OpsFlow(elem ~> flow.elem)
    }
    def -->(flow: DecisionFlow)(implicit builder: GraphBuilder): OpsFlow = {
      import builder.dslBuilder
      OpsFlow(elem ~> flow.elem)
    }
    def -->(flow: ConvergeFlow)(implicit builder: GraphBuilder): OpsFlow = {
      import builder.dslBuilder
      OpsFlow(elem ~> flow.elem)
    }
    def -->(flow: EndFlow)(implicit builder: GraphBuilder): Unit = {
      import builder.dslBuilder
      elem ~> Flow[Message].map(message => message.copy(message.meta.end)) ~> flow.elem
    }
  }

  case class DecisionFlow(elem: UniformFanOutShape[Message, Message]) extends StreamFlow {
    def condition(i: Int): ConditionFlow = ConditionFlow(elem.out(i))
    def -->(flow: TaskFlow)(implicit builder: GraphBuilder): OpsFlow = {
      import builder.dslBuilder
      OpsFlow(elem ~> flow.elem)
    }
    def -->(flow: ForkFlow)(implicit builder: GraphBuilder): OpsFlow = {
      import builder.dslBuilder
      OpsFlow(elem ~> flow.elem)
    }
    def -->(flow: JoinFlow)(implicit builder: GraphBuilder): OpsFlow = {
      import builder.dslBuilder
      OpsFlow(elem ~> flow.elem)
    }
    def -->(flow: DecisionFlow)(implicit builder: GraphBuilder): OpsFlow = {
      import builder.dslBuilder
      OpsFlow(elem ~> flow.elem)
    }
    def -->(flow: ConvergeFlow)(implicit builder: GraphBuilder): OpsFlow = {
      import builder.dslBuilder
      OpsFlow(elem ~> flow.elem)
    }
  }

  case class ConditionFlow(elem: Outlet[Message]) extends StreamFlow {
    def -->(flow: TaskFlow)(implicit builder: GraphBuilder): OpsFlow = {
      import builder.dslBuilder
      OpsFlow(elem ~> flow.elem)
    }
    def -->(flow: ForkFlow)(implicit builder: GraphBuilder): OpsFlow = {
      import builder.dslBuilder
      OpsFlow(elem ~> flow.elem)
    }
    def -->(flow: JoinFlow)(implicit builder: GraphBuilder): OpsFlow = {
      import builder.dslBuilder
      OpsFlow(elem ~> flow.elem)
    }
    def -->(flow: DecisionFlow)(implicit builder: GraphBuilder): OpsFlow = {
      import builder.dslBuilder
      OpsFlow(elem ~> flow.elem)
    }
    def -->(flow: ConvergeFlow)(implicit builder: GraphBuilder): OpsFlow = {
      import builder.dslBuilder
      OpsFlow(elem ~> flow.elem)
    }
  }

  case class ConvergeFlow(elem: UniformFanInShape[Message, Message]) extends StreamFlow {
    def -->(flow: TaskFlow)(implicit builder: GraphBuilder): OpsFlow = {
      import builder.dslBuilder
      OpsFlow(elem ~> flow.elem)
    }
    def -->(flow: ForkFlow)(implicit builder: GraphBuilder): OpsFlow = {
      import builder.dslBuilder
      OpsFlow(elem ~> flow.elem)
    }
    def -->(flow: JoinFlow)(implicit builder: GraphBuilder): OpsFlow = {
      import builder.dslBuilder
      OpsFlow(elem ~> flow.elem)
    }
    def -->(flow: DecisionFlow)(implicit builder: GraphBuilder): OpsFlow = {
      import builder.dslBuilder
      OpsFlow(elem ~> flow.elem)
    }
    def -->(flow: ConvergeFlow)(implicit builder: GraphBuilder): OpsFlow = {
      import builder.dslBuilder
      OpsFlow(elem ~> flow.elem)
    }
    def -->(flow: EndFlow)(implicit builder: GraphBuilder): Unit = {
      import builder.dslBuilder
      elem ~> Flow[Message].map(message => message.copy(message.meta.end)) ~> flow.elem
    }
  }

  case class StartFlow(elem: Source[Message, ActorRef]#Shape) extends StreamFlow {
    def -->(flow: TaskFlow)(implicit builder: GraphBuilder): OpsFlow = {
      import builder.dslBuilder
      OpsFlow(elem ~> flow.elem)
    }
    def -->(flow: ForkFlow)(implicit builder: GraphBuilder): OpsFlow = {
      import builder.dslBuilder
      OpsFlow(elem ~> flow.elem)
    }
    def -->(flow: DecisionFlow)(implicit builder: GraphBuilder): OpsFlow = {
      import builder.dslBuilder
      OpsFlow(elem ~> flow.elem)
    }
    def -->(flow: EndFlow)(implicit builder: GraphBuilder): Unit = {
      import builder.dslBuilder
      elem ~> Flow[Message].map(message => message.copy(message.meta.end)) ~> flow.elem
    }
  }

  case class EndFlow(elem: SinkShape[Message]) extends StreamFlow

  case class OpsFlow(elem: PortOps[Message]) extends StreamFlow {
    def -->(flow: TaskFlow)(implicit builder: GraphBuilder): OpsFlow = {
      import builder.dslBuilder
      OpsFlow(elem ~> flow.elem)
    }
    def -->(flow: ForkFlow)(implicit builder: GraphBuilder): OpsFlow = {
      import builder.dslBuilder
      OpsFlow(elem ~> flow.elem)
    }
    def -->(flow: JoinFlow)(implicit builder: GraphBuilder): OpsFlow = {
      import builder.dslBuilder
      OpsFlow(elem ~> flow.elem)
    }
    def -->(flow: DecisionFlow)(implicit builder: GraphBuilder): OpsFlow = {
      import builder.dslBuilder
      OpsFlow(elem ~> flow.elem)
    }
    def -->(flow: ConvergeFlow)(implicit builder: GraphBuilder): OpsFlow = {
      import builder.dslBuilder
      OpsFlow(elem ~> flow.elem)
    }
    def -->(flow: EndFlow)(implicit builder: GraphBuilder): Unit = {
      import builder.dslBuilder
      elem ~> Flow[Message].map(message => message.copy(message.meta.end)) ~> flow.elem
    }
  }
}
