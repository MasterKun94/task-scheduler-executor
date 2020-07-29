package com.oceanum.common

object TaskStatus extends Enumeration {
  type value = Value
  val OFFLINE: value = Value(0)
  val PREPARE: value = Value(1)
  val START: value = Value(2)
  val RUNNING: value = Value(3)
  val FAILED: value = Value(4)
  val SUCCESS: value = Value(5)
  val RETRY: value = Value(6)
  val TIMEOUT: value = Value(7)
  val KILL: value = Value(8)
}
