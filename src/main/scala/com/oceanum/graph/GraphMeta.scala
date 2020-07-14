package com.oceanum.graph

import com.oceanum.client.TaskMeta
import com.oceanum.cluster.exec.State
import com.oceanum.common.Meta

class GraphMeta(map: Map[String, Any]) extends Meta[GraphMeta](map) {
  def id: String = this("id")

  def operatorStatus: Map[String, TaskMeta] = this.get("operatorStatus").getOrElse(Map.empty[String, TaskMeta])

  def operatorStatus_+(state: State): GraphMeta = this + ("operatorStatus" -> (this.operatorStatus + (state.metadata.id -> state.metadata)))

  def fallbackStrategy: FallbackStrategy.value = this.get("fallbackStrategy").getOrElse(FallbackStrategy.RESUME)

  def graphStatus: GraphStatus.value = this.get("graphStatus").getOrElse(GraphStatus.OFFLINE)

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
