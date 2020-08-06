package com.oceanum.triger

import java.util.Date

/**
 * 触发器接口，Coordinator运行时会启动一个触发器任务，用以计划性的执行工作流。
 *
 * 自定义触发器的方式：实现该接口，并添加 {@li}
 *
 * @author chenmingkun
 * @see com.oceanum.triger.QuartzTrigger
 */
trait Trigger {
  def start(name: String, config: Map[String, String])(action: Date => Unit): Unit

  def stop(name: String): Boolean

  def suspend(name: String): Boolean

  def resume(name: String): Boolean

  def triggerType: String
}
