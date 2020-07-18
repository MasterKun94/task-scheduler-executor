package com.oceanum.cluster.tasks

import com.oceanum.cluster.exec.{StdHandler, TaskConfig}
import com.oceanum.common.Environment
import com.oceanum.common.StringParser.parseExpr

/**
 * @author chenmingkun
 * @date 2020/5/1
 */
case class ShellTaskConfig(cmd: Array[String] = Array.empty,
                           env: Map[String, String] = Map.empty,
                           directory: String = Environment.EXEC_WORK_DIR,
                           waitForTimeout: Long = -1,
                           stdoutHandler: StdHandler,
                           stderrHandler: StdHandler)
extends ProcessTaskConfig(cmd, env, directory, waitForTimeout, stdoutHandler, stderrHandler) {
  override def parseFunction(implicit exprEnv: Map[String, Any]): ShellTaskConfig = this.copy(
    cmd = cmd.map(parseExpr),
    env = env.map(kv => (parseExpr(kv._1), parseExpr(kv._2))),
    directory = parseExpr(directory)
  )
}
