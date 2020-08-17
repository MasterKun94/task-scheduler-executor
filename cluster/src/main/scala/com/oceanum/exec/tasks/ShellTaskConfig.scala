package com.oceanum.exec.tasks

import com.oceanum.common.Environment
import com.oceanum.jdbc.expr.ExprParser.parse
import com.oceanum.exec.StdHandler
import com.oceanum.jdbc.expr.JavaMap

/**
 * @author chenmingkun
 * @date 2020/5/1
 */
case class ShellTaskConfig(cmd: Array[String] = Array.empty,
                           env: Map[String, String] = Map.empty,
                           directory: String = Environment.EXEC_WORK_DIR,
                           waitForTimeout: String = "24h",
                           stdoutHandler: StdHandler,
                           stderrHandler: StdHandler)
extends ProcessTaskConfig(cmd, env, directory, waitForTimeout, stdoutHandler, stderrHandler) {
  override def parseFunction(implicit exprEnv: JavaMap[String, AnyRef]): ShellTaskConfig = this.copy(
    cmd = cmd.map(parse),
    env = env.map(kv => (parse(kv._1), parse(kv._2))),
    directory = parse(directory)
  )
}
