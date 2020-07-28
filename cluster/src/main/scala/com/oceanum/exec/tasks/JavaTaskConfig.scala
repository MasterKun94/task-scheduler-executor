package com.oceanum.exec.tasks

import com.oceanum.common.Environment
import com.oceanum.common.StringParser.parseExprRaw
import com.oceanum.exec.StdHandler
import com.oceanum.expr.JavaMap

/**
 * @author chenmingkun
 * @date 2020/5/1
 */
case class JavaTaskConfig(jars: Array[String],
                          mainClass: String,
                          args: Array[String] = Array.empty,
                          options: Array[String] = Array.empty,
                          env: Map[String, String] = Map.empty,
                          directory: String = Environment.EXEC_WORK_DIR,
                          waitForTimeout: String = "24h",
                          stdoutHandler: StdHandler,
                          stderrHandler: StdHandler)
  extends ProcessTaskConfig(
      (Environment.EXEC_JAVA +: options :+ "-cp" :+ jars.mkString(":") :+ mainClass) ++ args,
    env,
    directory,
    waitForTimeout,
    stdoutHandler,
    stderrHandler
  ) {
  override def parseFunction(implicit exprEnv: JavaMap[String, AnyRef]): JavaTaskConfig = this.copy(
    jars = jars.map(parseExprRaw),
    mainClass = parseExprRaw(mainClass),
    args = args.map(parseExprRaw),
    options = options.map(parseExprRaw),
    env = env.map(kv => (parseExprRaw(kv._1), parseExprRaw(kv._2))),
    directory = parseExprRaw(directory)
  )
  override def files: Seq[String] = jars.toSeq

  override def convert(fileMap: Map[String, String]): JavaTaskConfig = this.copy(
    jars = jars.map(fileMap.apply)
  )
}
