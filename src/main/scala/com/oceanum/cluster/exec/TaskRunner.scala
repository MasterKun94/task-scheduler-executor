package com.oceanum.cluster.exec

import com.oceanum.common.Log

/**
 * @author chenmingkun
 * @date 2020/4/29
 */
trait TaskRunner extends Log {

  def run(operatorProp: Operator[_ <: OperatorTask]): ExitCode

  def close: Unit
}
