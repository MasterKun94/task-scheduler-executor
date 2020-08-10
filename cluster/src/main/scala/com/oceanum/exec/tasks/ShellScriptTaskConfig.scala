package com.oceanum.exec.tasks

import com.oceanum.common.Environment
import com.oceanum.expr.ExprParser.parse
import com.oceanum.exec.StdHandler
import com.oceanum.expr.JavaMap

/**
 * @author chenmingkun
 * @date 2020/5/2
 */
case class ShellScriptTaskConfig(scriptFile: String,
                                 args: Array[String] = Array.empty,
                                 env: Map[String, String] = Map.empty,
                                 directory: String = Environment.EXEC_WORK_DIR,
                                 waitForTimeout: String = "24h",
                                 stdoutHandler: StdHandler,
                                 stderrHandler: StdHandler)
  extends ProcessTaskConfig(
        Environment.EXEC_SHELL +: args :+ scriptFile,
        env,
        directory,
        waitForTimeout,
        stdoutHandler,
        stderrHandler) {
      override def parseFunction(implicit exprEnv: JavaMap[String, AnyRef]): ShellScriptTaskConfig = this.copy(
            scriptFile = parse(scriptFile),
            args = args.map(parse),
            env = env.map(kv => (parse(kv._1), parse(kv._2))),
            directory = parse(directory)
      )

      override def fileSeq: Seq[String] = Seq(scriptFile)

      override def convert(fileMap: Map[String, String]): ShellScriptTaskConfig = this.copy(
            scriptFile = fileMap(scriptFile)
      )
}
