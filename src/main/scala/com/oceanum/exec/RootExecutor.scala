package com.oceanum.exec

import com.oceanum.common.Environment

/**
 * @author chenmingkun
 * @date 2020/5/9
 */
object RootExecutor extends  Executor {

  override def execute(operatorProp: Operator[_ <: OperatorTask]): ExitCode = {
    Environment.EXECUTORS.find(_.executable(operatorProp.prop)) match {
      case Some(executor) =>
        if (operatorProp.hook.isKilled) {
          ExitCode.KILL
        } else {
          executor.execute(operatorProp)
        }
      case None =>
        LOGGER.warning(s"no executable executor exists for prop ${operatorProp.prop.getClass}")
        ExitCode.UN_SUPPORT(operatorProp.prop.getClass)
    }
  }
}
