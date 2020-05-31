package com.oceanum.exec

/**
 * @author chenmingkun
 * @date 2020/5/30
 */
object State extends Enumeration {
  type State = Value
  val OFFLINE, PREPARE, START, RUNNING, FAILED, SUCCESS, RETRY, TIMEOUT, KILL = Value
}
