package com.oceanum.common

object GraphStatus extends Enumeration {
  type value = Value
  val OFFLINE, RUNNING, SUCCESS, EXCEPTION, FAILED, KILLED = Value
}
