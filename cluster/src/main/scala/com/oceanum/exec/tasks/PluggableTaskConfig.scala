package com.oceanum.exec.tasks

import com.oceanum.common.Environment
import com.oceanum.exec.StdHandler
import com.oceanum.expr.{ExprParser, JavaMap}

case class PluggableTaskConfig(args: Array[String] = Array.empty,
                               plugName: String,
                               files: Seq[String],
                               jars: Seq[String],
                               options: Array[String] = Array.empty,
                               env: Map[String, String] = Map.empty,
                               directory: String = Environment.EXEC_WORK_DIR,
                               waitForTimeout: String = "24h",
                               stdoutHandler: StdHandler,
                               stderrHandler: StdHandler)
  extends ProcessTaskConfig(
    (Environment.EXEC_JAVA +: options :+ "-cp" :+ (jars ++ Array()).mkString(":") :+ "com.oceanum.pluggable.Main") ++ args,
    env,
    directory,
    waitForTimeout,
    stdoutHandler,
    stderrHandler
  ) {
  override def parseFunction(implicit exprEnv: JavaMap[String, AnyRef]): ProcessTaskConfig = {
    this.copy(
      args = args.map(ExprParser.parse(_)),
      files = files.map(ExprParser.parse(_)),
      jars = jars.map(ExprParser.parse(_)),
      options = options.map(ExprParser.parse(_)),
      env = env.mapValues(ExprParser.parse(_))
    )
  }

  override def fileSeq: Seq[String] = files ++ jars

  override def convert(fileMap: Map[String, String]): ProcessTaskConfig = {
    this.copy(
      files = files.map(fileMap),
      jars = jars.map(fileMap)
    )
  }
}

