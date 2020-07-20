package com.oceanum.graph

import java.util.{Date, UUID}

import com.oceanum.client.{RichTaskMeta, TaskMeta}
import com.oceanum.cluster.exec.{FAILED, KILL, SUCCESS, State}
import com.oceanum.common.Meta

@SerialVersionUID(1L)
class RichGraphMeta(map: Map[String, Any]) extends Meta[RichGraphMeta](map) with GraphMeta[RichGraphMeta] {

  override def id: Int = this("id")

  def id_=(int: Int): RichGraphMeta = this + ("id" -> int)

  override def name: String = this("name")

  override def operators: Map[Int, TaskMeta[_]] = {
    this.get("operators").getOrElse(Map.empty[Int, TaskMeta[_]])
  }

  def addOperators(state: State): RichGraphMeta = {
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

  def reRunFlag: Boolean = this.get("reRunFlag").getOrElse(false)

  def reRunFlag_=(flag: Boolean): RichGraphMeta = this + ("reRunFlag" -> flag)

  override def graphStatus: GraphStatus.value = {
    this.get("graphStatus").getOrElse(GraphStatus.OFFLINE)
  }

  def graphStatus_=(status: GraphStatus.value): RichGraphMeta = {
    this + ("graphStatus" -> status)
  }

  def updateGraphStatus(status: GraphStatus.value): RichGraphMeta = {
    this + ("graphStatus" -> Seq(graphStatus, status).maxBy(_.id))
  }

  def merge(meta: RichGraphMeta): RichGraphMeta = {
    val keys: Set[Int] = this.operators.keySet ++ meta.operators.keySet
    val map: Map[Int, TaskMeta[_]] = keys.map { key => {
      val task = (this.operators.get(key), meta.operators.get(key)) match {
        case (Some(o1), Some(o2)) => if (o1.createTime.before(o2.createTime)) o2 else o1
        case (None, Some(o2)) => o2
        case (Some(o1), None) => o1
        case (None, None) => throw new IllegalArgumentException
      }
      (key, task.asInstanceOf[RichTaskMeta])
    }}.toMap

    this + ("operators" -> map) updateGraphStatus meta.graphStatus
  }

  override def error: Throwable = {
    this("error")
  }

  def error_=(e: Throwable): RichGraphMeta = {
    this + ("error" -> e) updateGraphStatus GraphStatus.FAILED
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

  override def createTime: Date = this("createTime")

  def createTime_=(date: Date): RichGraphMeta = this + ("createTime" -> date)

  override def scheduleTime: Date = this("scheduleTime")

  def scheduleTime_=(date: Date): RichGraphMeta = this + ("scheduleTime" -> date)

  override def startTime: Date = this("startTime")

  def startTime_=(date: Date): RichGraphMeta = this + ("startTime" -> date)

  override def endTime: Date = this("endTime")

  override def env: Map[String, Any] = this.get("env").getOrElse(Map.empty)

  def addEnv(kv: (String, Any)): RichGraphMeta = this + ("env", this.env + kv)

  def addEnv(right: Map[String, Any]): RichGraphMeta = this + ("env", this.env ++ right)
}

object RichGraphMeta {

  def apply(map: Map[String, Any]): RichGraphMeta = {
    val meta = new RichGraphMeta(map).scheduleTime = new Date()
    if (meta.name != null && meta.name.trim.nonEmpty) {
      meta
    } else {
      meta + ("name" -> UUID.randomUUID().toString)
    }
  }

  def create(name: String): RichGraphMeta = RichGraphMeta("name" -> name)

  def apply(elems: (String, Any)*): RichGraphMeta = RichGraphMeta(Map(elems: _*))
}