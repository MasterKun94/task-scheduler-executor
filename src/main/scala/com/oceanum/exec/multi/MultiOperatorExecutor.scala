package com.oceanum.exec.multi

import com.oceanum.exec.ExitCode.{ERROR, KILL, OK, UN_SUPPORT}
import com.oceanum.exec._

/**
 * @author chenmingkun
 * @date 2020/5/10
 */
class MultiOperatorExecutor extends Executor[MultiOperatorProp] {
  override protected def typedExecute(operatorProp: Operator[_ <: MultiOperatorProp]): ExitCode = {
    for (elem <- operatorProp.prop.props) {
      val operator = operatorProp.copy(prop = elem)
      RootExecutor.execute(operator) match {
        case ERROR =>
          return ERROR
        case KILL =>
          return KILL
        case code: UN_SUPPORT =>
          return code
        case OK =>
          // continue
      }
    }
    OK
  }

  override def executable(p: OperatorProp): Boolean = p.isInstanceOf[MultiOperatorProp]
}
