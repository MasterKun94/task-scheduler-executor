package com.oceanum.graph

/**
 * @author chenmingkun
 * @date 2020/7/15
 */
object ReRunStrategy extends Enumeration {
  type value = Value

  val NONE, RUN_ALL, RUN_ONLY_FAILED, RUN_ALL_AFTER_FAILED = Value

}
