package com.oceanum.expr

import java.util.Date

import com.googlecode.aviator.runtime.`type`.{AviatorLong, AviatorObject, AviatorRuntimeJavaType, AviatorString}
import com.googlecode.aviator.runtime.function.AbstractFunction
import com.oceanum.common.{ExprContext, TaskMeta}

/**
 * @author chenmingkun
 * @date 2020/7/18
 */
class TaskIdFunction extends AbstractFunction {
  override def getName: String = "task.id"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = {
    AviatorLong.valueOf(env.get(ExprContext.taskKey).asInstanceOf[TaskMeta].id)
  }
}

class TaskCreateTimeFunction extends AbstractFunction {
  override def getName: String = "task.createTime"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = {
    val date: Date = env.get(ExprContext.taskKey).asInstanceOf[TaskMeta].createTime
    AviatorRuntimeJavaType.valueOf(date)
  }
}

class TaskStartTimeFunction extends AbstractFunction {
  override def getName: String = "task.startTime"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = {
    val date: Date = env.get(ExprContext.taskKey).asInstanceOf[TaskMeta].startTime
    AviatorRuntimeJavaType.valueOf(date)
  }
}

class TaskUserFunction extends AbstractFunction {
  override def getName: String = "task.user"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = {
    new AviatorString(env.get(ExprContext.taskKey).asInstanceOf[TaskMeta].user)
  }
}

class TaskTypeFunction extends AbstractFunction {
  override def getName: String = "task.type"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = {
    new AviatorString(env.get(ExprContext.taskKey).asInstanceOf[TaskMeta].taskType)
  }
}

class TaskExecDirFunction extends AbstractFunction {
  override def getName: String = "task.execDir"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = {
    new AviatorString(env.get(ExprContext.taskKey).asInstanceOf[TaskMeta].execDir)
  }
}