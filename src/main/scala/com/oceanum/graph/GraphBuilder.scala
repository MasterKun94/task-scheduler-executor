package com.oceanum.graph

import akka.stream.SinkShape
import akka.stream.scaladsl.{Source, _}
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.{Done, NotUsed}

import scala.concurrent.Future

trait GraphBuilder {
  def build(implicit source: Source[GraphMeta, NotUsed], sink: SinkShape[GraphMeta], builder: GraphDSL.Builder[Future[Done]])
}
