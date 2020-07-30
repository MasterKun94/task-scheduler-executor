package com.oceanum.serialize

import java.util.concurrent.atomic.AtomicBoolean

import com.oceanum.client.Task
import com.oceanum.common.{GraphContext, RichGraphMeta, RichTaskMeta}
import com.oceanum.graph.GraphDefine
import com.oceanum.graph.Operators.{Converge, Decision, End, Fork, Join, Start, TaskOperator}

object SerializeUtil {

  def defaultSerialization: Serialization[_] = DefaultJsonSerialization

  private val isInit: AtomicBoolean = new AtomicBoolean(false)

  def init(): Unit = {
    if (isInit.getAndSet(true))
      return

    defaultSerialization.register[RichGraphMeta]("GRAPH_META")
    defaultSerialization.register[RichTaskMeta]("TASK_META")
    defaultSerialization.register[GraphContext]("GRAPH_CONTEXT")
    defaultSerialization.register[Task]("TASK")
    defaultSerialization.register[GraphDefine]("GRAPH_DEFINE")
    defaultSerialization.register[TaskOperator]("TASK_OPERATOR")
    defaultSerialization.register[Fork]("FORK_OPERATOR")
    defaultSerialization.register[Join]("JOIN_OPERATOR")
    defaultSerialization.register[Decision]("DECISION_OPERATOR")
    defaultSerialization.register[Converge]("CONVERGE_OPERATOR")
    defaultSerialization.register[Start]("START_OPERATOR")
    defaultSerialization.register[End]("END_OPERATOR")
  }
}
