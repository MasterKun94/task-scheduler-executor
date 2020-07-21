package com.oceanum.exec.tasks

import com.oceanum.exec.StdHandler
import com.oceanum.common.Environment
import com.oceanum.common.StringParser.parseExprRaw
import com.oceanum.expr.JavaMap

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
  override def parseFunction(implicit exprEnv: JavaMap[String, AnyRef]): ShellTaskConfig = this.copy(
    cmd = cmd.map(parseExprRaw),
    env = env.map(kv => (parseExprRaw(kv._1), parseExprRaw(kv._2))),
    directory = parseExprRaw(directory)
  )
}
