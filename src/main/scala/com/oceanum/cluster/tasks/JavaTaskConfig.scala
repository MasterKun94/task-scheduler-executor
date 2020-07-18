package com.oceanum.cluster.tasks

import com.oceanum.cluster.exec.{StdHandler, TaskConfig}
import com.oceanum.common.Environment
import com.oceanum.common.StringParser.parseExpr

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
                          waitForTimeout: Long = -1,
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
  override def parseFunction(implicit exprEnv: Map[String, Any]): JavaTaskConfig = this.copy(
    jars = jars.map(parseExpr),
    mainClass = parseExpr(mainClass),
    args = args.map(parseExpr),
    options = options.map(parseExpr),
    env = env.map(kv => (parseExpr(kv._1), parseExpr(kv._2))),
    directory = parseExpr(directory)
  )
  override def files: Seq[String] = jars.toSeq

  override def convert(fileMap: Map[String, String]): JavaTaskConfig = this.copy(
    jars = jars.map(fileMap.apply)
  )
}
