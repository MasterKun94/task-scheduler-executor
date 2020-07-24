package com.oceanum.exec.tasks

import com.oceanum.exec.StdHandler
import com.oceanum.common.Environment
import com.oceanum.common.StringParser.parseExprRaw
import com.oceanum.expr.JavaMap

/**
 * @author chenmingkun
 * @date 2020/5/1
 */
case class PythonTaskConfig(pyFile: String,
                            args: Array[String] = Array.empty,
                            env: Map[String, String] = Map.empty,
                            directory: String = Environment.EXEC_WORK_DIR,
                            waitForTimeout: String = "24h",
                            stdoutHandler: StdHandler,
                            stderrHandler: StdHandler)
  extends ProcessTaskConfig(
    Environment.EXEC_PYTHON +: pyFile +: args,
    env,
    directory,
    waitForTimeout,
    stdoutHandler,
    stderrHandler) {
  override def parseFunction(implicit exprEnv: JavaMap[String, AnyRef]): PythonTaskConfig = this.copy(
    pyFile = parseExprRaw(pyFile),
    args = args.map(parseExprRaw),
    env = env.map(kv => (parseExprRaw(kv._1), parseExprRaw(kv._2))),
    directory = parseExprRaw(directory)
  )
  override def files: Seq[String] = Seq(pyFile)

  override def convert(fileMap: Map[String, String]): PythonTaskConfig = this.copy(
    pyFile = fileMap(pyFile)
  )
}
