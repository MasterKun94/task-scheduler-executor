package com.oceanum.graph

import akka.{Done, NotUsed}
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.{Outlet, SinkShape, UniformFanInShape, UniformFanOutShape}
import akka.stream.scaladsl.{Flow, GraphDSL, Source}

import scala.concurrent.Future

trait Operator {

}

object Operator {

  case class TaskFlow(elem: Flow[GraphMeta, GraphMeta, NotUsed]) extends Operator

  case class Fork(elem: UniformFanOutShape[GraphMeta, GraphMeta]) extends Operator {
    def ==>(operator: TaskFlow)(implicit builder: GraphDSL.Builder[Future[Done]]): Ops = Ops(elem ~> operator.elem)
    def ==>(operator: Fork)(implicit builder: GraphDSL.Builder[Future[Done]]): Ops = Ops(elem ~> operator.elem)
    def ==>(operator: Join)(implicit builder: GraphDSL.Builder[Future[Done]]): Ops = Ops(elem ~> operator.elem)
    def ==>(operator: Decision)(implicit builder: GraphDSL.Builder[Future[Done]]): Ops = Ops(elem ~> operator.elem)
    def ==>(operator: Return)(implicit builder: GraphDSL.Builder[Future[Done]]): Ops = Ops(elem ~> operator.elem)
  }

  case class Join(elem: UniformFanInShape[GraphMeta, GraphMeta]) extends Operator

  case class Decision(elem: UniformFanOutShape[GraphMeta, GraphMeta]) extends Operator {
    def out(i: Int): OutPort = OutPort(elem.out(i))
  }

  case class OutPort(elem: Outlet[GraphMeta]) extends Operator {
    def ==>(operator: TaskFlow)(implicit builder: GraphDSL.Builder[Future[Done]]): Ops = Ops(elem ~> operator.elem)
    def ==>(operator: Fork)(implicit builder: GraphDSL.Builder[Future[Done]]): Ops = Ops(elem ~> operator.elem)
    def ==>(operator: Join)(implicit builder: GraphDSL.Builder[Future[Done]]): Ops = Ops(elem ~> operator.elem)
    def ==>(operator: Decision)(implicit builder: GraphDSL.Builder[Future[Done]]): Ops = Ops(elem ~> operator.elem)
    def ==>(operator: Return)(implicit builder: GraphDSL.Builder[Future[Done]]): Ops = Ops(elem ~> operator.elem)
  }

  case class Return(elem: UniformFanInShape[GraphMeta, GraphMeta]) extends Operator

  case class Start(elem: Source[GraphMeta, NotUsed]) extends Operator {
    def ==>(operator: TaskFlow)(implicit builder: GraphDSL.Builder[Future[Done]]): Ops = Ops(elem ~> operator.elem)
    def ==>(operator: Fork)(implicit builder: GraphDSL.Builder[Future[Done]]): Ops = Ops(elem ~> operator.elem)
    def ==>(operator: Decision)(implicit builder: GraphDSL.Builder[Future[Done]]): Ops = Ops(elem ~> operator.elem)
    def ==>(operator: End)(implicit builder: GraphDSL.Builder[Future[Done]]): Unit = elem ~> operator.elem
  }

  case class End(elem: SinkShape[GraphMeta])

  case class Ops(elem: PortOps[GraphMeta]) extends Operator {
    def ==>(operator: TaskFlow)(implicit builder: GraphDSL.Builder[Future[Done]]): Ops = Ops(elem ~> operator.elem)
    def ==>(operator: Fork)(implicit builder: GraphDSL.Builder[Future[Done]]): Ops = Ops(elem ~> operator.elem)
    def ==>(operator: Join)(implicit builder: GraphDSL.Builder[Future[Done]]): Ops = Ops(elem ~> operator.elem)
    def ==>(operator: Decision)(implicit builder: GraphDSL.Builder[Future[Done]]): Ops = Ops(elem ~> operator.elem)
    def ==>(operator: Return)(implicit builder: GraphDSL.Builder[Future[Done]]): Ops = Ops(elem ~> operator.elem)
    def ==>(operator: End)(implicit builder: GraphDSL.Builder[Future[Done]]): Unit = elem ~> operator.elem
  }
}
