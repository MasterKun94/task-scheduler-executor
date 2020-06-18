package com.oceanum.exec.executors

import com.oceanum.exec.ExitCode.{ERROR, KILL, OK, UN_SUPPORT}
import com.oceanum.exec._
import com.oceanum.exec.tasks.MultiOperatorTask

/**
 * @author chenmingkun
 * @date 2020/5/10
 */
//class MultiOperatorExecutor extends TypedExecutor[MultiOperatorTask] {
//  override protected def typedExecute(operatorProp: Operator[_ <: MultiOperatorTask]): ExitCode = {
//    for (elem <- operatorProp.prop.props) {
//      val operator = operatorProp.copy(prop = elem)
//      RootExecutor.execute(operator) match {
//        case ERROR =>
//          return ERROR
//        case KILL =>
//          return KILL
//        case code: UN_SUPPORT =>
//          return code
//        case OK =>
//          // continue
//      }
//    }
//    OK
//  }
//
//  override def executable(p: OperatorTask): Boolean = p.isInstanceOf[MultiOperatorTask]
//}
