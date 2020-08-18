package com.oceanum.exec.tasks

import com.oceanum.exec.StdHandler
import com.oceanum.expr.ExprParser
import com.oceanum.expr.JavaMap

/**
 * @author chenmingkun
 * @date 2020/7/7
 */
object SysTasks {
  case class UserAddTaskConfig(user: String) extends ProcessTaskConfig(
    propCmd = Array("sudo", "useradd", user),
    propStdoutHandler = StdHandler.empty,
    propStderrHandler = StdHandler.empty
  ) {
    override def parseFunction(implicit exprEnv: JavaMap[String, AnyRef]): ProcessTaskConfig = this.copy(
      user = ExprParser.parse(user)
    )
  }
}
