package com.oceanum.graph

import java.util.{Date, UUID}

import com.oceanum.client.{RichTaskMeta, TaskMeta}
import com.oceanum.cluster.exec.{FAILED, KILL, SUCCESS, State}

@SerialVersionUID(1L)
class NewRichGraphMeta(id: Int = 0,
                       name: String = null,
                       operators: Map[Int, TaskMeta[_]] = Map.empty,
                       fallbackStrategy: FallbackStrategy.value = FallbackStrategy.CONTINUE,
                       reRunStrategy: ReRunStrategy.value = ReRunStrategy.NONE,
                       graphStatus: GraphStatus.value = GraphStatus.OFFLINE,
                       error: Throwable = null,
                       createTime: Date = null,
                       scheduleTime: Date = null,
                       startTime: Date = null,
                       endTime: Date = null,
                       env: Map[String, Any] = Map.empty,
                       reRunFlag: Boolean = false
                      )
  extends NewGraphMeta(
    id = id,
    name = name,
    operators = operators,
    fallbackStrategy = fallbackStrategy,
    reRunStrategy = reRunStrategy,
    graphStatus = graphStatus,
    error = error,
    createTime = createTime,
    scheduleTime = scheduleTime,
    startTime = startTime,
    endTime = endTime,
    env = env) {

  def copy(id: Int = id,
           name: String = name,
           operators: Map[Int, TaskMeta[_]] = operators,
           fallbackStrategy: FallbackStrategy.value = fallbackStrategy,
           reRunStrategy: ReRunStrategy.value = reRunStrategy,
           graphStatus: GraphStatus.value = graphStatus,
           error: Throwable = error,
           createTime: Date = createTime,
           scheduleTime: Date = scheduleTime,
           startTime: Date = startTime,
           endTime: Date = endTime,
           env: Map[String, Any] = env,
           rerunFlag: Boolean = reRunFlag): NewRichGraphMeta = {
    new NewRichGraphMeta(
      id = id,
      name = name,
      operators = operators,
      fallbackStrategy = fallbackStrategy,
      reRunStrategy = reRunStrategy,
      graphStatus = graphStatus,
      error = error,
      createTime = createTime,
      scheduleTime = scheduleTime,
      startTime = startTime,
      endTime = endTime,
      env = env,
      rerunFlag)
  }

  def addOperators(state: State): NewRichGraphMeta = {
    val graphStatus = state match {
      case _: SUCCESS => GraphStatus.RUNNING
      case _: FAILED => GraphStatus.EXCEPTION
      case _: KILL => GraphStatus.KILLED
      case _ => GraphStatus.RUNNING
    }
    val metadata = state.metadata.asInstanceOf[RichTaskMeta]
    val tuple = metadata.id -> metadata
    updateGraphStatus(graphStatus).copy(operators = this.operators + tuple)
  }

  def updateGraphStatus(status: GraphStatus.value): NewRichGraphMeta = {
    this.copy(graphStatus = Seq(graphStatus, status).maxBy(_.id))
  }

  def merge(meta: NewGraphMeta): NewRichGraphMeta = {
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
    updateGraphStatus(meta.graphStatus).copy(operators = map)
  }

  def end: NewRichGraphMeta = {
    val meta = graphStatus match {
      case GraphStatus.RUNNING => this.updateGraphStatus(GraphStatus.SUCCESS)

      case GraphStatus.EXCEPTION => this.updateGraphStatus(GraphStatus.FAILED)

      case _: GraphStatus.value => this
    }
    meta.copy(endTime = new Date())
  }

  def addEnv(kv: (String, Any)): NewRichGraphMeta = this.copy(env = this.env + kv)

  def addEnv(right: Map[String, Any]): NewRichGraphMeta = this.copy(env = this.env ++ right)
}

object NewRichGraphMeta {

  def create(name: String): NewRichGraphMeta = new NewRichGraphMeta(name = name)
}

