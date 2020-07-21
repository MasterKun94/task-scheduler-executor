package com.oceanum.graph

/**
 * @author chenmingkun
 * @date 2020/7/14
 */
object GraphStatus extends Enumeration {
  type value = Value
  val OFFLINE: GraphStatus.value = Value(1)
  val RUNNING: GraphStatus.value = Value(2)
  val SUCCESS: GraphStatus.value = Value(3)
  val EXCEPTION: GraphStatus.value = Value(4)
  val FAILED: GraphStatus.value = Value(5)
  val KILLED: GraphStatus.value = Value(6)
}
