package com.oceanum.cluster.tasks

/**
 * @author chenmingkun
 * @date 2020/5/2
 */
case class SuUserTask(user: String, prop: ProcessTask)
  extends ProcessTask(
    Array("su", "-", user, "-c") :+ s"${prop.propCmd.mkString(" ")}",
    prop.propEnv,
    prop.propDirectory,
    prop.propWaitForTimeout,
    prop.propStdoutHandler,
    prop.propStderrHandler
  )
