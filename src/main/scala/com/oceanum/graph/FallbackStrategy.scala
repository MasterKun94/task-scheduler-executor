package com.oceanum.graph

/**
 * @author chenmingkun
 * @date 2020/7/14
 */
object FallbackStrategy extends Enumeration {
  type value = Value
  val RESUME, STOP = Value
}
