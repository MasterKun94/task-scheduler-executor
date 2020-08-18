package com.oceanum.exec.tasks

import com.oceanum.common.Environment
import com.oceanum.expr.ExprParser.parse
import com.oceanum.exec.StdHandler
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
    pyFile = parse(pyFile),
    args = args.map(parse),
    env = env.map(kv => (parse(kv._1), parse(kv._2))),
    directory = parse(directory)
  )
  override def fileSeq: Seq[String] = Seq(pyFile)

  override def convert(fileMap: Map[String, String]): PythonTaskConfig = this.copy(
    pyFile = fileMap(pyFile)
  )
}
