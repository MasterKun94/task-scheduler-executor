package com.oceanum.graph

import java.util.Date

import com.oceanum.client.{RichTaskMeta, TaskMeta}
import com.oceanum.cluster.exec.{FAILED, KILL, SUCCESS, State}
import com.oceanum.common.Meta

class RichGraphMeta(map: Map[String, Any]) extends Meta[RichGraphMeta](map) with GraphMeta[RichGraphMeta] {
  override def id: String = this("id")

  override def operators: Map[Int, TaskMeta[_]] = {
    this.get("operators").getOrElse(Map.empty[Int, TaskMeta[_]])
  }

  def operators_+(state: State): RichGraphMeta = {
    val graphStatus = state match {
      case _: SUCCESS => GraphStatus.RUNNING
      case _: FAILED => GraphStatus.EXCEPTION
      case _: KILL => GraphStatus.KILLED
      case _ => GraphStatus.RUNNING
    }
    val metadata = state.metadata.asInstanceOf[RichTaskMeta]
    val tuple = metadata.id -> metadata
    updateGraphStatus(graphStatus) + ("operators" -> (this.operators + tuple))
  }

  override def fallbackStrategy: FallbackStrategy.value = {
    this.get("fallbackStrategy").getOrElse(FallbackStrategy.CONTINUE)
  }

  def fallbackStrategy_=(strategy: FallbackStrategy.value): RichGraphMeta = {
    this + ("fallbackStrategy" -> strategy)
  }

  override def reRunStrategy: ReRunStrategy.value = this.get("reRunStrategy").getOrElse(ReRunStrategy.NONE)

  def reRunStrategy_=(reRunStrategy: ReRunStrategy.value): RichGraphMeta = {
    this + ("reRunStrategy" -> reRunStrategy)
  }

  def reRunFlag: Boolean = this("reRunFlag")

  def reRunFlag_=(flag: Boolean): RichGraphMeta = this + ("reRunFlag" -> (this.reRunFlag || flag))

  override def graphStatus: GraphStatus.value = {
    this.get("graphStatus").getOrElse(GraphStatus.OFFLINE)
  }

  def updateGraphStatus(status: GraphStatus.value): RichGraphMeta = {
    this + ("graphStatus" -> Seq(graphStatus, status).maxBy(_.id))
  }

  def merge(meta: RichGraphMeta): RichGraphMeta = {
    this + ("operators" -> (this.operators ++ meta.operators)) updateGraphStatus meta.graphStatus
  }

  override def error: Throwable = {
    this("error")
  }

  def error_=(e: Throwable): RichGraphMeta = {
    this + ("error" -> e) updateGraphStatus GraphStatus.FAILED
  }


  def start: RichGraphMeta = {
    this + ("startTime" -> new Date()) updateGraphStatus GraphStatus.RUNNING
  }

  def end: RichGraphMeta = {
    val meta = graphStatus match {
      case GraphStatus.RUNNING => this.updateGraphStatus(GraphStatus.SUCCESS)

      case GraphStatus.EXCEPTION => this.updateGraphStatus(GraphStatus.FAILED)

      case _: GraphStatus.value => this
    }
    meta + ("endTime" -> new Date())
  }

  override def toString: String = s"GraphMeta(${map.toArray.map(t => t._1 + ": " + t._2).mkString(", ")})"

  override def createTime: Date = this("startTime")

  override def startTime: Date = this("startTime")

  override def endTime: Date = this("endTime")
}

object RichGraphMeta {

  def apply(map: Map[String, Any]): RichGraphMeta = new RichGraphMeta(map + ("createTime" -> new Date()))

  def apply(elems: (String, Any)*): RichGraphMeta = RichGraphMeta(Map(elems: _*))
}
