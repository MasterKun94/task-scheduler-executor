package com.oceanum.graph

trait GraphMetaHandler {
  def handle(richGraphMeta: RichGraphMeta): Unit
}

object GraphMetaHandler {
  def default(): GraphMetaHandler = new GraphMetaHandler {
    override def handle(richGraphMeta: RichGraphMeta): Unit = println("graphMeta: " + richGraphMeta)
  }

  def apply(handler: RichGraphMeta => Unit): GraphMetaHandler = new GraphMetaHandler {
    override def handle(richGraphMeta: RichGraphMeta): Unit = handler(richGraphMeta)
  }
}