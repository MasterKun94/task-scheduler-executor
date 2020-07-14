package com.oceanum.graph

import akka.Done
import akka.stream.scaladsl._
import com.oceanum.graph.Operator.{End, Start}

import scala.concurrent.Future

trait GraphBuilder {
  def build(implicit start: Start, end: End, builder: GraphDSL.Builder[Future[Done]])
}
