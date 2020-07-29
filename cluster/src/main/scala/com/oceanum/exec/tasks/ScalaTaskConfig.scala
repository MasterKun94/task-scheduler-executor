package com.oceanum.exec.tasks

import com.oceanum.common.Environment
import com.oceanum.expr.ExprParser.parse
import com.oceanum.exec.StdHandler
import com.oceanum.expr.JavaMap

/**
 * @author chenmingkun
 * @date 2020/5/1
 */
case class ScalaTaskConfig(jars: Array[String],
                           mainClass: String,
                           args: Array[String] = Array.empty,
                           options: Array[String] = Array.empty,
                           env: Map[String, String] = Map.empty,
                           directory: String = Environment.EXEC_WORK_DIR,
                           waitForTimeout: String = "24h",
                           stdoutHandler: StdHandler,
                           stderrHandler: StdHandler)
  extends ProcessTaskConfig(
    (Environment.EXEC_SCALA +: options :+ "-cp" :+ jars.mkString(":") :+ mainClass) ++ args,
    env,
    directory,
    waitForTimeout,
    stdoutHandler,
    stderrHandler
  ) {
  override def parseFunction(implicit exprEnv: JavaMap[String, AnyRef]): ScalaTaskConfig = this.copy(
    jars = jars.map(parse),
    mainClass = parse(mainClass),
    args = args.map(parse),
    options = options.map(parse),
    env = env.map(kv => (parse(kv._1), parse(kv._2))),
    directory = parse(directory)
  )
  override def files: Seq[String] = jars.toSeq

  override def convert(fileMap: Map[String, String]): ScalaTaskConfig = this.copy(
    jars = jars.map(fileMap.apply)
  )
}
