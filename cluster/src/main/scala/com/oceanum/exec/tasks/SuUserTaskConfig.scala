package com.oceanum.exec.tasks
import com.oceanum.expr.ExprParser
import com.oceanum.expr.JavaMap

/**
 * @author chenmingkun
 * @date 2020/5/2
 */
case class SuUserTaskConfig(user: String, prop: ProcessTaskConfig)
  extends ProcessTaskConfig(
    Array("sudo", "su", "-", user, "-c") :+ s"${prop.propCmd.mkString(" ")}",
    prop.propEnv,
    prop.propDirectory,
    prop.propWaitForTimeout,
    prop.propStdoutHandler,
    prop.propStderrHandler
  ) {
  override def parseFunction(implicit exprEnv: JavaMap[String, AnyRef]): SuUserTaskConfig = this.copy(
    user = ExprParser.parse(user),
    prop = prop.parseFunction(exprEnv)
  )

  override def fileSeq: Seq[String] = prop.fileSeq

  override def convert(fileMap: Map[String, String]): SuUserTaskConfig = this.copy(
    prop = prop.convert(fileMap)
  )
}
