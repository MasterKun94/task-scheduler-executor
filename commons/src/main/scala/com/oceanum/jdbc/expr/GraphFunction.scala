package com.oceanum.jdbc.expr

import java.util.Date

import com.googlecode.aviator.runtime.`type`._
import com.googlecode.aviator.runtime.function.{AbstractFunction, FunctionUtils}
import com.oceanum.annotation.IFunction
import com.oceanum.common.{GraphContext, GraphMeta}

/**
 * @author chenmingkun
 * @date 2020/7/18
 */
@IFunction
class GraphIdFunction extends AbstractFunction {
  override def getName: String = "graph.id"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = {
    AviatorLong.valueOf(env.get(GraphContext.graphKey).asInstanceOf[GraphMeta].id)
  }
}

@IFunction
class GraphRerunIdFunction extends AbstractFunction {
  override def getName: String = "graph.rerunId"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = {
    AviatorLong.valueOf(env.get(GraphContext.graphKey).asInstanceOf[GraphMeta].rerunId)
  }
}

@IFunction
class GraphNameFunction extends AbstractFunction {
  override def getName: String = "graph.name"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = {
    new AviatorString(env.get(GraphContext.graphKey).asInstanceOf[GraphMeta].name)
  }
}

@IFunction
class GraphCreateTimeFunction extends AbstractFunction {
  override def getName: String = "graph.createTime"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = {
    val date: Date = env.get(GraphContext.graphKey).asInstanceOf[GraphMeta].createTime.orNull
    AviatorRuntimeJavaType.valueOf(date)
  }
}

@IFunction
class GraphScheduleTimeFunction extends AbstractFunction {
  override def getName: String = "graph.scheduleTime"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = {
    val date: Date = env.get(GraphContext.graphKey).asInstanceOf[GraphMeta].scheduleTime.orNull
    AviatorRuntimeJavaType.valueOf(date)
  }
}

@IFunction
class GraphStartTimeFunction extends AbstractFunction {
  override def getName: String = "graph.startTime"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = {
    val date: Date = env.get(GraphContext.graphKey).asInstanceOf[GraphMeta].startTime.orNull
    AviatorRuntimeJavaType.valueOf(date)
  }
}

@IFunction
class GraphFindTaskFunction extends AbstractFunction {
  override def getName: String = "graph.task"

  override def call(env: JavaMap[String, AnyRef], name: AviatorObject): AviatorObject = {
    val str = FunctionUtils.getStringValue(name, env)
    env.get(GraphContext.graphKey).asInstanceOf[GraphMeta]
      .tasks
      .find(_._2.name.startsWith(str))
      .map(t => AviatorRuntimeJavaType.valueOf(t._2))
      .getOrElse(AviatorNil.NIL)
  }
}

@IFunction
class GraphLatestTaskStateFunction extends AbstractFunction {
  override def getName: String = "graph.latestTask"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = {
    env.get(GraphContext.graphKey).asInstanceOf[GraphMeta].latestTask match {
      case Some(value) => AviatorRuntimeJavaType.valueOf(value)
      case None => AviatorNil.NIL
    }
  }
}
