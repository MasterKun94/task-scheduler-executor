package com.oceanum.exec.process

/**
 * @author chenmingkun
 * @date 2020/5/2
 */
case class SuUserProp(user: String, prop: ProcessProp)
  extends ProcessProp(
    Array("su", "-", user, "-c") :+ s"${prop.propCmd.mkString(" ")}",
    prop.propEnv,
    prop.propDirectory,
    prop.propWaitForTimeout,
    prop.propStdoutHandler,
    prop.propStderrHandler
  )
