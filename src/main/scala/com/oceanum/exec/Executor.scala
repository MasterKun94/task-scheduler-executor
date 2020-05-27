package com.oceanum.exec

import com.oceanum.common.Log

/**
 * @author chenmingkun
 * @date 2020/4/29
 */
trait Executor[T <: OperatorProp] extends Log {

  def execute(operatorProp: Operator[_ <: OperatorProp]): ExitCode = {
    if (executable(operatorProp.prop)) {
      operatorProp.eventListener.start()
      typedExecute(operatorProp.asInstanceOf[Operator[T]])
    } else {
      ExitCode.UN_SUPPORT(operatorProp.prop.getClass)
    }
  }

  protected def typedExecute(operatorProp: Operator[_ <: T]): ExitCode

  def executable(p: OperatorProp): Boolean
}
