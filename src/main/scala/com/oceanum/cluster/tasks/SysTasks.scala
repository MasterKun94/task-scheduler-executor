package com.oceanum.cluster.tasks

import com.oceanum.cluster.exec.StdHandler
import com.oceanum.common.StringParser

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
    override def parseFunction(implicit exprEnv: Map[String, Any]): ProcessTaskConfig = this.copy(
      user = StringParser.parseExpr(user)
    )
  }
}
