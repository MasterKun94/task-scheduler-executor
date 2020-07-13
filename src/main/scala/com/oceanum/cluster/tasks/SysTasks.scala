package com.oceanum.cluster.tasks

import com.oceanum.cluster.exec.StdHandler

/**
 * @author chenmingkun
 * @date 2020/7/7
 */
object SysTasks {
  case class UserAddTaskConfig(user: String) extends ProcessTaskConfig(
    propCmd = Array("sudo", "useradd", user),
    propStdoutHandler = StdHandler.empty,
    propStderrHandler = StdHandler.empty
  )
}
