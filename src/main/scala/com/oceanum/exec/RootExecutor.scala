package com.oceanum.exec

import com.oceanum.common.{Environment, Log}

/**
 * @author chenmingkun
 * @date 2020/5/9
 */
object RootExecutor extends Log {

  def execute(operatorProp: Operator[_ <: OperatorProp]): ExitCode = {
    Environment.EXECUTORS.find(_.executable(operatorProp.prop)) match {
      case Some(executor) =>
        if (operatorProp.hook.isKilled) {
          ExitCode.KILL
        } else {
          executor.execute(operatorProp)
        }
      case None =>
        LOGGER.warn(s"no executable executor exists for prop ${operatorProp.prop.getClass}")
        ExitCode.UN_SUPPORT(operatorProp.prop.getClass)
    }
  }
}
