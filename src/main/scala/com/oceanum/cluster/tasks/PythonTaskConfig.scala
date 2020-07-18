package com.oceanum.cluster.tasks

import com.oceanum.cluster.exec.StdHandler
import com.oceanum.common.Environment
import com.oceanum.common.StringParser.parseExpr

/**
 * @author chenmingkun
 * @date 2020/5/1
 */
case class PythonTaskConfig(pyFile: String,
                            args: Array[String] = Array.empty,
                            env: Map[String, String] = Map.empty,
                            directory: String = Environment.EXEC_WORK_DIR,
                            waitForTimeout: Long = -1,
                            stdoutHandler: StdHandler,
                            stderrHandler: StdHandler)
  extends ProcessTaskConfig(
    Environment.EXEC_PYTHON +: pyFile +: args,
    env,
    directory,
    waitForTimeout,
    stdoutHandler,
    stderrHandler) {
  override def parseFunction(implicit exprEnv: Map[String, Any]): PythonTaskConfig = this.copy(
    pyFile = parseExpr(pyFile),
    args = args.map(parseExpr),
    env = env.map(kv => (parseExpr(kv._1), parseExpr(kv._2))),
    directory = parseExpr(directory)
  )
  override def files: Seq[String] = Seq(pyFile)

  override def convert(fileMap: Map[String, String]): PythonTaskConfig = this.copy(
    pyFile = fileMap(pyFile)
  )
}
