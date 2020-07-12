package com.oceanum.cluster.tasks

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
  )
