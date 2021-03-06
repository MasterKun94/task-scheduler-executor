package com.oceanum.expr

import java.util.Date

import com.googlecode.aviator.runtime.`type`.{AviatorLong, AviatorNil, AviatorObject, AviatorRuntimeJavaType, AviatorString}
import com.googlecode.aviator.runtime.function.{AbstractFunction, FunctionUtils}
import com.oceanum.annotation.IFunction
import com.oceanum.common.{GraphContext, TaskMeta}

/**
 * @author chenmingkun
 * @date 2020/7/18
 */
@IFunction
class TaskIdFunction extends AbstractFunction {
  override def getName: String = "task.id"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = {
    AviatorLong.valueOf(env.get(GraphContext.taskKey).asInstanceOf[TaskMeta].id)
  }

  override def call(env: JavaMap[String, AnyRef], task: AviatorObject): AviatorObject = {
    if (task.isNull(env))
      AviatorNil.NIL
    else {
      AviatorLong.valueOf(FunctionUtils.getJavaObject(task, env).asInstanceOf[TaskMeta].id)
    }
  }
}

@IFunction
class TaskRerunIdFunction extends AbstractFunction {
  override def getName: String = "task.rerunId"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = {
    AviatorLong.valueOf(env.get(GraphContext.taskKey).asInstanceOf[TaskMeta].rerunId)
  }

  override def call(env: JavaMap[String, AnyRef], task: AviatorObject): AviatorObject = {
    if (task.isNull(env))
      AviatorNil.NIL
    else {
      AviatorLong.valueOf(FunctionUtils.getJavaObject(task, env).asInstanceOf[TaskMeta].rerunId)
    }
  }
}

@IFunction
class TaskNameFunction extends AbstractFunction {
  override def getName: String = "task.name"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = {
    new AviatorString(env.get(GraphContext.taskKey).asInstanceOf[TaskMeta].name)
  }

  override def call(env: JavaMap[String, AnyRef], task: AviatorObject): AviatorObject = {
    if (task.isNull(env))
      AviatorNil.NIL
    else {
      new AviatorString(FunctionUtils.getJavaObject(task, env).asInstanceOf[TaskMeta].name)
    }
  }
}

@IFunction
class TaskCreateTimeFunction extends AbstractFunction {
  override def getName: String = "task.createTime"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = {
    val date: Date = env.get(GraphContext.taskKey).asInstanceOf[TaskMeta].createTime.get
    AviatorRuntimeJavaType.valueOf(date)
  }

  override def call(env: JavaMap[String, AnyRef], task: AviatorObject): AviatorObject = {
    if (task.isNull(env))
      AviatorNil.NIL
    else {
      AviatorRuntimeJavaType.valueOf((FunctionUtils.getJavaObject(task, env).asInstanceOf[TaskMeta].createTime.orNull))
    }
  }
}

@IFunction
class TaskStartTimeFunction extends AbstractFunction {
  override def getName: String = "task.startTime"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = {
    val date: Date = env.get(GraphContext.taskKey).asInstanceOf[TaskMeta].startTime.get
    AviatorRuntimeJavaType.valueOf(date)
  }

  override def call(env: JavaMap[String, AnyRef], task: AviatorObject): AviatorObject = {
    if (task.isNull(env))
      AviatorNil.NIL
    else {
      AviatorRuntimeJavaType.valueOf((FunctionUtils.getJavaObject(task, env).asInstanceOf[TaskMeta].startTime.orNull))
    }
  }
}


@IFunction
class TaskEndTimeFunction extends AbstractFunction {
  override def getName: String = "task.endTime"

  override def call(env: JavaMap[String, AnyRef], task: AviatorObject): AviatorObject = {
    if (task.isNull(env))
      AviatorNil.NIL
    else {
      AviatorRuntimeJavaType.valueOf((FunctionUtils.getJavaObject(task, env).asInstanceOf[TaskMeta].endTime.orNull))
    }
  }
}

@IFunction
class TaskUserFunction extends AbstractFunction {
  override def getName: String = "task.user"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = {
    new AviatorString(env.get(GraphContext.taskKey).asInstanceOf[TaskMeta].user)
  }

  override def call(env: JavaMap[String, AnyRef], task: AviatorObject): AviatorObject = {
    if (task.isNull(env))
      AviatorNil.NIL
    else {
      new AviatorString((FunctionUtils.getJavaObject(task, env).asInstanceOf[TaskMeta].user))
    }
  }
}

@IFunction
class TaskTypeFunction extends AbstractFunction {
  override def getName: String = "task.type"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = {
    new AviatorString(env.get(GraphContext.taskKey).asInstanceOf[TaskMeta].taskType)
  }

  override def call(env: JavaMap[String, AnyRef], task: AviatorObject): AviatorObject = {
    if (task.isNull(env))
      AviatorNil.NIL
    else {
      new AviatorString((FunctionUtils.getJavaObject(task, env).asInstanceOf[TaskMeta].taskType))
    }
  }
}

@IFunction
class TaskExecDirFunction extends AbstractFunction {
  override def getName: String = "task.execDir"

  override def call(env: JavaMap[String, AnyRef]): AviatorObject = {
    new AviatorString(env.get(GraphContext.taskKey).asInstanceOf[TaskMeta].execDir)
  }

  override def call(env: JavaMap[String, AnyRef], task: AviatorObject): AviatorObject = {
    if (task.isNull(env))
      AviatorNil.NIL
    else {
      new AviatorString((FunctionUtils.getJavaObject(task, env).asInstanceOf[TaskMeta].execDir))
    }
  }
}