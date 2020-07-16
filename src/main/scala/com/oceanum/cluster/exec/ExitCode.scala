package com.oceanum.cluster.exec

/**
 * @author chenmingkun
 * @date 2020/5/9
 */
class ExitCode {}

object ExitCode {

  def apply(code: Int): ExitCode = {
    if (code == 0) OK
    else ERROR("exit with code " + code)
  }

  case object OK extends ExitCode
  case object KILL extends ExitCode
  case class ERROR(message: String) extends ExitCode
  case class UN_SUPPORT(taskType: String) extends ExitCode
}
