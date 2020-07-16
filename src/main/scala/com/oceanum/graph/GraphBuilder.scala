package com.oceanum.graph

import java.util.concurrent.atomic.{AtomicInteger, AtomicReference}

import com.oceanum.graph.Operator.{End, Start}

class GraphBuilder(val start: Start,
                   val end: End,
                   implicit protected[graph] val dslBuilder: DslBuilder,
                   private val handler: GraphMetaHandler) {

  private val taskIdValue: AtomicInteger = new AtomicInteger(0)

  def idValue: Int = taskIdValue.getAndIncrement()

  lazy protected[graph] val metaHandler: GraphMetaHandler = new GraphMetaHandler {
    private val metaRef: AtomicReference[RichGraphMeta] = new AtomicReference[RichGraphMeta]()
    override def handle(richGraphMeta: RichGraphMeta): Unit = {
      val meta = if (metaRef.get() == null) {
        metaRef.set(richGraphMeta)
        richGraphMeta
      } else {
        metaRef.getAndSet(metaRef.get().merge(richGraphMeta))
      }
      handler.handle(meta)
    }
  }
}

object GraphBuilder {
  def apply(start: Start, end: End, dslBuilder: DslBuilder, handler: GraphMetaHandler): GraphBuilder = {
    new GraphBuilder(start, end, dslBuilder, handler)
  }
}