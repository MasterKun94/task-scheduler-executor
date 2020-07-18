package com.oceanum.expr

import java.util.Date

import com.googlecode.aviator.runtime.`type`.{AviatorLong, AviatorObject, AviatorRuntimeJavaType, AviatorString}
import com.googlecode.aviator.runtime.function.AbstractFunction
import com.oceanum.common.Implicits.EnvHelper.graphKey
import com.oceanum.graph.GraphMeta

/**
 * @author chenmingkun
 * @date 2020/7/18
 */
class GraphIdFunction extends AbstractFunction {
  override def getName: String = "graph.id"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = {
    AviatorLong.valueOf(env.get(graphKey).asInstanceOf[GraphMeta[_]].id)
  }
}

class GraphNameFunction extends AbstractFunction {
  override def getName: String = "graph.name"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = {
    new AviatorString(env.get(graphKey).asInstanceOf[GraphMeta[_]].name)
  }
}

class GraphCreateTimeFunction extends AbstractFunction {
  override def getName: String = "graph.createTime"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = {
    val date: Date = env.get(graphKey).asInstanceOf[GraphMeta[_]].createTime
    AviatorRuntimeJavaType.valueOf(date)
  }
}

class GraphScheduleTimeFunction extends AbstractFunction {
  override def getName: String = "graph.scheduleTime"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = {
    val date: Date = env.get(graphKey).asInstanceOf[GraphMeta[_]].scheduleTime
    AviatorRuntimeJavaType.valueOf(date)
  }
}

class GraphStartTimeFunction extends AbstractFunction {
  override def getName: String = "graph.startTime"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = {
    val date: Date = env.get(graphKey).asInstanceOf[GraphMeta[_]].startTime
    AviatorRuntimeJavaType.valueOf(date)
  }
}
