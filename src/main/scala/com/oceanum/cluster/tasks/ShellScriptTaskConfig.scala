package com.oceanum.cluster.tasks

import com.oceanum.cluster.exec.{StdHandler, TaskConfig}
import com.oceanum.common.Environment
import com.oceanum.common.StringParser.parseExpr

/**
 * @author chenmingkun
 * @date 2020/5/2
 */
case class ShellScriptTaskConfig(scriptFile: String,
                                 args: Array[String] = Array.empty,
                                 env: Map[String, String] = Map.empty,
                                 directory: String = Environment.EXEC_WORK_DIR,
                                 waitForTimeout: Long = -1,
                                 stdoutHandler: StdHandler,
                                 stderrHandler: StdHandler)
  extends ProcessTaskConfig(
        Environment.EXEC_SHELL +: args :+ scriptFile,
        env,
        directory,
        waitForTimeout,
        stdoutHandler,
        stderrHandler) {
      override def parseFunction(implicit exprEnv: Map[String, Any]): ShellScriptTaskConfig = this.copy(
            scriptFile = parseExpr(scriptFile),
            args = args.map(parseExpr),
            env = env.map(kv => (parseExpr(kv._1), parseExpr(kv._2))),
            directory = parseExpr(directory)
      )

      override def files: Seq[String] = Seq(scriptFile)

      override def convert(fileMap: Map[String, String]): ShellScriptTaskConfig = this.copy(
            scriptFile = fileMap(scriptFile)
      )
}
