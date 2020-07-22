package com.oceanum.expr

import java.util.Date

import com.googlecode.aviator.runtime.`type`.{AviatorLong, AviatorObject, AviatorRuntimeJavaType, AviatorString}
import com.googlecode.aviator.runtime.function.AbstractFunction
import com.oceanum.common.{ExprContext, GraphMeta}

/**
 * @author chenmingkun
 * @date 2020/7/18
 */
class GraphIdFunction extends AbstractFunction {
  override def getName: String = "graph.id"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = {
    AviatorLong.valueOf(env.get(ExprContext.graphKey).asInstanceOf[GraphMeta].id)
  }
}

class GraphNameFunction extends AbstractFunction {
  override def getName: String = "graph.name"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = {
    new AviatorString(env.get(ExprContext.graphKey).asInstanceOf[GraphMeta].name)
  }
}

class GraphCreateTimeFunction extends AbstractFunction {
  override def getName: String = "graph.createTime"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = {
    val date: Date = env.get(ExprContext.graphKey).asInstanceOf[GraphMeta].createTime
    AviatorRuntimeJavaType.valueOf(date)
  }
}

class GraphScheduleTimeFunction extends AbstractFunction {
  override def getName: String = "graph.scheduleTime"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = {
    val date: Date = env.get(ExprContext.graphKey).asInstanceOf[GraphMeta].scheduleTime
    AviatorRuntimeJavaType.valueOf(date)
  }
}

class GraphStartTimeFunction extends AbstractFunction {
  override def getName: String = "graph.startTime"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = {
    val date: Date = env.get(ExprContext.graphKey).asInstanceOf[GraphMeta].startTime
    AviatorRuntimeJavaType.valueOf(date)
  }
}
