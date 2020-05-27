package com.oceanum.exec

/**
 * @author chenmingkun
 * @date 2020/5/1
 */
trait ExecutorHook {
  def kill(): Boolean

  def isKilled: Boolean
}
