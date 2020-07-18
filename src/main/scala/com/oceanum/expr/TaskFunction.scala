package com.oceanum.expr

import java.util.Date

import com.googlecode.aviator.runtime.`type`.{AviatorLong, AviatorObject, AviatorRuntimeJavaType, AviatorString}
import com.googlecode.aviator.runtime.function.AbstractFunction
import com.oceanum.client.TaskMeta
import com.oceanum.common.Implicits.EnvHelper.taskKey

/**
 * @author chenmingkun
 * @date 2020/7/18
 */
class TaskIdFunction extends AbstractFunction {
  override def getName: String = "task.id"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = {
    AviatorLong.valueOf(env.get(taskKey).asInstanceOf[TaskMeta[_]].id)
  }
}

class TaskCreateTimeFunction extends AbstractFunction {
  override def getName: String = "task.createTime"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = {
    val date: Date = env.get(taskKey).asInstanceOf[TaskMeta[_]].createTime
    AviatorRuntimeJavaType.valueOf(date)
  }
}

class TaskStartTimeFunction extends AbstractFunction {
  override def getName: String = "task.startTime"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = {
    val date: Date = env.get(taskKey).asInstanceOf[TaskMeta[_]].startTime
    AviatorRuntimeJavaType.valueOf(date)
  }
}

class TaskUserFunction extends AbstractFunction {
  override def getName: String = "task.user"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = {
    new AviatorString(env.get(taskKey).asInstanceOf[TaskMeta[_]].user)
  }
}

class TaskTypeFunction extends AbstractFunction {
  override def getName: String = "task.type"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = {
    new AviatorString(env.get(taskKey).asInstanceOf[TaskMeta[_]].taskType)
  }
}

class TaskExecDirFunction extends AbstractFunction {
  override def getName: String = "task.execDir"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = {
    new AviatorString(env.get(taskKey).asInstanceOf[TaskMeta[_]].execDir)
  }
}