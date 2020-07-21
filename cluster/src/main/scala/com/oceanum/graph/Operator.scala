package com.oceanum.graph

import akka.NotUsed
import akka.actor.ActorRef
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{Flow, Source}
import akka.stream.{Outlet, SinkShape, UniformFanInShape, UniformFanOutShape}

trait Operator {

}

object Operator {

  case class TaskFlow(elem: Flow[RichGraphMeta, RichGraphMeta, NotUsed]) extends Operator

  case class Fork(elem: UniformFanOutShape[RichGraphMeta, RichGraphMeta]) extends Operator {
    def -->(operator: TaskFlow)(implicit builder: GraphBuilder): Ops = {
      import builder.dslBuilder
      Ops(elem ~> operator.elem)
    }
    def -->(operator: Fork)(implicit builder: GraphBuilder): Ops = {
      import builder.dslBuilder
      Ops(elem ~> operator.elem)
    }
    def -->(operator: Join)(implicit builder: GraphBuilder): Ops = {
      import builder.dslBuilder
      Ops(elem ~> operator.elem)
    }
    def -->(operator: Decision)(implicit builder: GraphBuilder): Ops = {
      import builder.dslBuilder
      Ops(elem ~> operator.elem)
    }
    def -->(operator: Converge)(implicit builder: GraphBuilder): Ops = {
      import builder.dslBuilder
      Ops(elem ~> operator.elem)
    }
  }

  case class Join(elem: UniformFanInShape[RichGraphMeta, RichGraphMeta]) extends Operator {
    def -->(operator: TaskFlow)(implicit builder: GraphBuilder): Ops = {
      import builder.dslBuilder
      Ops(elem ~> operator.elem)
    }
    def -->(operator: Fork)(implicit builder: GraphBuilder): Ops = {
      import builder.dslBuilder
      Ops(elem ~> operator.elem)
    }
    def -->(operator: Join)(implicit builder: GraphBuilder): Ops = {
      import builder.dslBuilder
      Ops(elem ~> operator.elem)
    }
    def -->(operator: Decision)(implicit builder: GraphBuilder): Ops = {
      import builder.dslBuilder
      Ops(elem ~> operator.elem)
    }
    def -->(operator: Converge)(implicit builder: GraphBuilder): Ops = {
      import builder.dslBuilder
      Ops(elem ~> operator.elem)
    }
    def -->(operator: End)(implicit builder: GraphBuilder): Unit = {
      import builder.dslBuilder
      elem ~> Flow[RichGraphMeta].map(_.end) ~> operator.elem
    }
  }

  case class Decision(elem: UniformFanOutShape[RichGraphMeta, RichGraphMeta]) extends Operator {
    def condition(i: Int): Condition = Condition(elem.out(i))
    def -->(operator: TaskFlow)(implicit builder: GraphBuilder): Ops = {
      import builder.dslBuilder
      Ops(elem ~> operator.elem)
    }
    def -->(operator: Fork)(implicit builder: GraphBuilder): Ops = {
      import builder.dslBuilder
      Ops(elem ~> operator.elem)
    }
    def -->(operator: Join)(implicit builder: GraphBuilder): Ops = {
      import builder.dslBuilder
      Ops(elem ~> operator.elem)
    }
    def -->(operator: Decision)(implicit builder: GraphBuilder): Ops = {
      import builder.dslBuilder
      Ops(elem ~> operator.elem)
    }
    def -->(operator: Converge)(implicit builder: GraphBuilder): Ops = {
      import builder.dslBuilder
      Ops(elem ~> operator.elem)
    }
  }

  case class Condition(elem: Outlet[RichGraphMeta]) extends Operator {
    def -->(operator: TaskFlow)(implicit builder: GraphBuilder): Ops = {
      import builder.dslBuilder
      Ops(elem ~> operator.elem)
    }
    def -->(operator: Fork)(implicit builder: GraphBuilder): Ops = {
      import builder.dslBuilder
      Ops(elem ~> operator.elem)
    }
    def -->(operator: Join)(implicit builder: GraphBuilder): Ops = {
      import builder.dslBuilder
      Ops(elem ~> operator.elem)
    }
    def -->(operator: Decision)(implicit builder: GraphBuilder): Ops = {
      import builder.dslBuilder
      Ops(elem ~> operator.elem)
    }
    def -->(operator: Converge)(implicit builder: GraphBuilder): Ops = {
      import builder.dslBuilder
      Ops(elem ~> operator.elem)
    }
  }

  case class Converge(elem: UniformFanInShape[RichGraphMeta, RichGraphMeta]) extends Operator {
    def -->(operator: TaskFlow)(implicit builder: GraphBuilder): Ops = {
      import builder.dslBuilder
      Ops(elem ~> operator.elem)
    }
    def -->(operator: Fork)(implicit builder: GraphBuilder): Ops = {
      import builder.dslBuilder
      Ops(elem ~> operator.elem)
    }
    def -->(operator: Join)(implicit builder: GraphBuilder): Ops = {
      import builder.dslBuilder
      Ops(elem ~> operator.elem)
    }
    def -->(operator: Decision)(implicit builder: GraphBuilder): Ops = {
      import builder.dslBuilder
      Ops(elem ~> operator.elem)
    }
    def -->(operator: Converge)(implicit builder: GraphBuilder): Ops = {
      import builder.dslBuilder
      Ops(elem ~> operator.elem)
    }
    def -->(operator: End)(implicit builder: GraphBuilder): Unit = {
      import builder.dslBuilder
      elem ~> Flow[RichGraphMeta].map(_.end) ~> operator.elem
    }
  }

  case class Start(elem: Source[RichGraphMeta, ActorRef]#Shape) extends Operator {
    def -->(operator: TaskFlow)(implicit builder: GraphBuilder): Ops = {
      import builder.dslBuilder
      Ops(elem ~> operator.elem)
    }
    def -->(operator: Fork)(implicit builder: GraphBuilder): Ops = {
      import builder.dslBuilder
      Ops(elem ~> operator.elem)
    }
    def -->(operator: Decision)(implicit builder: GraphBuilder): Ops = {
      import builder.dslBuilder
      Ops(elem ~> operator.elem)
    }
    def -->(operator: End)(implicit builder: GraphBuilder): Unit = {
      import builder.dslBuilder
      elem ~> Flow[RichGraphMeta].map(_.end) ~> operator.elem
    }
  }

  case class End(elem: SinkShape[RichGraphMeta])

  case class Ops(elem: PortOps[RichGraphMeta]) extends Operator {
    def -->(operator: TaskFlow)(implicit builder: GraphBuilder): Ops = {
      import builder.dslBuilder
      Ops(elem ~> operator.elem)
    }
    def -->(operator: Fork)(implicit builder: GraphBuilder): Ops = {
      import builder.dslBuilder
      Ops(elem ~> operator.elem)
    }
    def -->(operator: Join)(implicit builder: GraphBuilder): Ops = {
      import builder.dslBuilder
      Ops(elem ~> operator.elem)
    }
    def -->(operator: Decision)(implicit builder: GraphBuilder): Ops = {
      import builder.dslBuilder
      Ops(elem ~> operator.elem)
    }
    def -->(operator: Converge)(implicit builder: GraphBuilder): Ops = {
      import builder.dslBuilder
      Ops(elem ~> operator.elem)
    }
    def -->(operator: End)(implicit builder: GraphBuilder): Unit = {
      import builder.dslBuilder
      elem ~> Flow[RichGraphMeta].map(_.end) ~> operator.elem
    }
  }
}
