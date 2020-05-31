package com.oceanum.exec

import com.oceanum.common.Log

/**
 * @author chenmingkun
 * @date 2020/5/30
 */
trait TypedExecutor[T <: OperatorTask] extends Executor with Log {

    def execute(operatorProp: Operator[_ <: OperatorTask]): ExitCode = {
      if (executable(operatorProp.prop)) {
        operatorProp.eventListener.start()
        typedExecute(operatorProp.asInstanceOf[Operator[T]])
      } else {
        ExitCode.UN_SUPPORT(operatorProp.prop.getClass)
      }
    }

    protected def typedExecute(operatorProp: Operator[_ <: T]): ExitCode

    def executable(p: OperatorTask): Boolean

}
