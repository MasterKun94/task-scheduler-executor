package com.oceanum.graph

import java.util.concurrent.atomic.AtomicInteger

import com.oceanum.graph.StreamFlows.{EndFlow, StartFlow}

class GraphBuilder(val start: StartFlow,
                   val end: EndFlow,
                   implicit protected[graph] val dslBuilder: DslBuilder,
                   val handler: GraphMetaHandler) {

  private val taskIdValue: AtomicInteger = new AtomicInteger(0)

  def idValue: Int = taskIdValue.getAndIncrement()
}

object GraphBuilder {
  def apply(start: StartFlow, end: EndFlow, dslBuilder: DslBuilder, handler: GraphMetaHandler): GraphBuilder = {
    new GraphBuilder(start, end, dslBuilder, handler)
  }
}