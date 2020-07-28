package com.oceanum.exec.tasks

import com.oceanum.common.Environment
import com.oceanum.exec.StdHandler
import com.oceanum.expr.JavaMap

case class PluggableTaskConfig(args: Array[String] = Array.empty,
                               options: Array[String] = Array.empty,
                               env: Map[String, String] = Map.empty,
                               directory: String = Environment.EXEC_WORK_DIR,
                               waitForTimeout: String = "24h",
                               stdoutHandler: StdHandler,
                               stderrHandler: StdHandler)
  extends ProcessTaskConfig (
    Array(Environment.EXEC_JAVA),
    env,
    directory,
    waitForTimeout,
    stdoutHandler,
    stderrHandler
  ) {
  override def parseFunction(implicit exprEnv: JavaMap[String, AnyRef]): ProcessTaskConfig = ???
}
