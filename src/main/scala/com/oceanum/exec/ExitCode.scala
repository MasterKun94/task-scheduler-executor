package com.oceanum.exec

/**
 * @author chenmingkun
 * @date 2020/5/9
 */
class ExitCode {}

object ExitCode {

  def apply(code: Int): ExitCode = {
    if (code == 0) OK
    else if (code == 1) ERROR
    else throw new IllegalArgumentException
  }

  case object OK extends ExitCode
  case object ERROR extends ExitCode
  case object KILL extends ExitCode
  case class UN_SUPPORT(operatorClass: Class[_ <: OperatorProp]) extends ExitCode
}
