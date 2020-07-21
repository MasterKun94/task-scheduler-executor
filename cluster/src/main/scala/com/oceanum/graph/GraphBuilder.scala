package com.oceanum.graph

import java.util.concurrent.atomic.AtomicInteger

import com.oceanum.graph.Operator.{End, Start}

class GraphBuilder(val start: Start,
                   val end: End,
                   implicit protected[graph] val dslBuilder: DslBuilder,
                   val handler: GraphMetaHandler) {

  private val taskIdValue: AtomicInteger = new AtomicInteger(0)

  def idValue: Int = taskIdValue.getAndIncrement()
}

object GraphBuilder {
  def apply(start: Start, end: End, dslBuilder: DslBuilder, handler: GraphMetaHandler): GraphBuilder = {
    new GraphBuilder(start, end, dslBuilder, handler)
  }
}