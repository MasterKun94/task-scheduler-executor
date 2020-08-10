package com.oceanum.exec

/**
 * @author chenmingkun
 * @date 2020/5/9
 */
class ExitCode {}

object ExitCode {

  def apply(code: Int): ExitCode = {
    if (code == 0) OK
//    else if (code == -1) KILL
    else ERROR(new Exception("exit with code " + code))
  }

  case object OK extends ExitCode
  case object KILL extends ExitCode
  case class ERROR(e: Throwable) extends ExitCode
  case class UN_SUPPORT(taskType: String) extends ExitCode
}
