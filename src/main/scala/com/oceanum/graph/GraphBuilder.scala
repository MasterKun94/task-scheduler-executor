package com.oceanum.graph

import akka.stream.scaladsl._
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.{Done, NotUsed}

import scala.concurrent.Future

class GraphBuilder {
  private val source: Source[GraphMeta, NotUsed] = Source.single(GraphMeta())
  private val sink: Sink[GraphMeta, Future[Done]] = Sink.foreach(println)
  private val iterable = Seq.newBuilder[GraphDSL.Builder[Future[Done]] => Unit]
  val flow: Flow[GraphMeta, GraphMeta, NotUsed] = FlowFactory.create(null)(null)

  def add(build: GraphDSL.Builder[Future[Done]] => Unit): Unit = iterable += build

  def add(from: Flow[GraphMeta, GraphMeta, NotUsed], to: Flow[GraphMeta, GraphMeta, NotUsed]): Unit = iterable += (b => (from ~> to)(b))

  def addSource(flow: Flow[GraphMeta, GraphMeta, NotUsed]): Unit = iterable += (b => (source ~> flow)(b))

  def init: Unit = RunnableGraph fromGraph GraphDSL.create(sink) { implicit builder: GraphDSL.Builder[Future[Done]] => resultSink =>
    iterable.result().foreach(_(builder))
}
