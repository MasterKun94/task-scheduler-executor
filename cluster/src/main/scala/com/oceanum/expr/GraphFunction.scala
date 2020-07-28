package com.oceanum.expr

import java.util.Date

import com.googlecode.aviator.runtime.`type`.{AviatorLong, AviatorNil, AviatorObject, AviatorRuntimeJavaType, AviatorString}
import com.googlecode.aviator.runtime.function.{AbstractFunction, FunctionUtils}
import com.oceanum.common.{GraphContext, GraphMeta, RichGraphMeta}

/**
 * @author chenmingkun
 * @date 2020/7/18
 */
class GraphIdFunction extends AbstractFunction {
  override def getName: String = "graph.id"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = {
    AviatorLong.valueOf(env.get(GraphContext.graphKey).asInstanceOf[GraphMeta].id)
  }
}

class GraphNameFunction extends AbstractFunction {
  override def getName: String = "graph.name"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = {
    new AviatorString(env.get(GraphContext.graphKey).asInstanceOf[GraphMeta].name)
  }
}

class GraphCreateTimeFunction extends AbstractFunction {
  override def getName: String = "graph.createTime"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = {
    val date: Date = env.get(GraphContext.graphKey).asInstanceOf[GraphMeta].createTime
    AviatorRuntimeJavaType.valueOf(date)
  }
}

class GraphScheduleTimeFunction extends AbstractFunction {
  override def getName: String = "graph.scheduleTime"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = {
    val date: Date = env.get(GraphContext.graphKey).asInstanceOf[GraphMeta].scheduleTime
    AviatorRuntimeJavaType.valueOf(date)
  }
}

class GraphStartTimeFunction extends AbstractFunction {
  override def getName: String = "graph.startTime"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = {
    val date: Date = env.get(GraphContext.graphKey).asInstanceOf[GraphMeta].startTime
    AviatorRuntimeJavaType.valueOf(date)
  }
}

class GraphFindTaskFunction extends AbstractFunction {
  override def getName: String = "graph.findTask"

  override def call(env: JavaMap[String, AnyRef], name: AviatorObject): AviatorObject = {
    val str = FunctionUtils.getStringValue(name, env)
    env.get(GraphContext.graphKey).asInstanceOf[GraphMeta]
      .tasks
      .find(_._2.name.startsWith(str))
      .map(t => AviatorRuntimeJavaType.valueOf(t._2))
      .getOrElse(AviatorNil.NIL)
  }
}

class GraphLatestTaskStateFunction extends AbstractFunction {
  override def getName: String = "graph.latestTask"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = {
    val task = env.get(GraphContext.graphKey).asInstanceOf[RichGraphMeta].latestTask
    if (task == null) {
      AviatorNil.NIL
    } else {
      AviatorRuntimeJavaType.valueOf(task)
    }
  }
}
