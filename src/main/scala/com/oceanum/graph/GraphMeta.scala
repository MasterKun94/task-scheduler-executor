package com.oceanum.graph

import com.oceanum.cluster.exec.State
import com.oceanum.common.Meta

class GraphMeta(map: Map[String, Any]) extends Meta[GraphMeta](map) {
  def id: String = this("id")

  def operatorStatus: Map[String, State] = map.getOrElse("operatorStatus", Map.empty[String, State]).asInstanceOf[Map[String, State]]

  def operatorStatus_+(state: State): GraphMeta = this + ("operatorStatus" -> (this.operatorStatus + (state.meta.id -> state)))

  def merge(meta: GraphMeta): GraphMeta = {
    this + ("operatorStatus" -> (this.operatorStatus ++ meta.operatorStatus))
    //TODO
  }

  override def toString: String = s"GraphMeta(${map.toArray.map(t => t._1 + ": " + t._2).mkString(", ")})"
}

object GraphMeta {
  def apply(map: Map[String, Any]): GraphMeta = new GraphMeta(map)

  def apply(elems: (String, Any)*): GraphMeta = new GraphMeta(Map(elems: _*))
}
