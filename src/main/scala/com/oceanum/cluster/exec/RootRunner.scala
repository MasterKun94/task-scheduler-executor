package com.oceanum.cluster.exec

import com.oceanum.common.Environment

/**
 * @author chenmingkun
 * @date 2020/5/9
 */
object RootRunner extends  TaskRunner {

  override def run(operatorProp: Operator[_ <: OperatorTask]): ExitCode = {
    Environment.TASK_RUNNERS.find(_.executable(operatorProp.prop)) match {
      case Some(executor) =>
        if (operatorProp.hook.isKilled) {
          ExitCode.KILL
        } else {
          executor.run(operatorProp)
        }
      case None =>
        LOGGER.warning(s"no executable executor exists for prop ${operatorProp.prop.getClass}")
        ExitCode.UN_SUPPORT(operatorProp.prop.getClass)
    }
  }
}
