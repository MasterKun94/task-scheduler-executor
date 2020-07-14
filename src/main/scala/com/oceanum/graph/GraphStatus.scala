package com.oceanum.graph

/**
 * @author chenmingkun
 * @date 2020/7/14
 */
object GraphStatus extends Enumeration {
  type value = Value
  val OFFLINE, START, RUNNING, SUCCESS, FAILED, EXCEPTION = Value

}
