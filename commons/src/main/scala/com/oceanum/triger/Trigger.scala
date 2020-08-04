package com.oceanum.triger

import java.util.Date

/**
 * @author chenmingkun
 * @date 2020/7/26
 */
trait Trigger {
  def start(name: String, config: Map[String, String])(action: Date => Unit): Unit

  def stop(name: String): Boolean

  def suspend(name: String): Boolean

  def resume(name: String): Boolean

  def triggerType: String
}
