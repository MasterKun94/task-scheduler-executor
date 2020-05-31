package com.oceanum.exec

import com.oceanum.common.Log

/**
 * @author chenmingkun
 * @date 2020/4/29
 */
trait Executor extends Log {

  def execute(operatorProp: Operator[_ <: OperatorTask]): ExitCode
}
