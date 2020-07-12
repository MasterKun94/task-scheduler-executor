package com.oceanum.cluster.tasks

import com.oceanum.cluster.exec.InputStreamHandler

/**
 * @author chenmingkun
 * @date 2020/7/7
 */
object SysTasks {
  case class UserAddTaskConfig(user: String) extends ProcessTaskConfig(
    propCmd = Array("sudo", "useradd", user),
    propStdoutHandler = InputStreamHandler.empty,
    propStderrHandler = InputStreamHandler.empty
  )
}
