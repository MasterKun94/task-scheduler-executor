package com.oceanum.trigger

import java.util.Date

/**
 * 触发器接口，Coordinator运行时会启动一个触发器任务，用以计划性的执行工作流。
 *
 * 自定义触发器的方式：实现该接口，并添加注解 {@link com.oceanum.annotation.ITrigger}
 *
 * @author chenmingkun
 * @see com.oceanum.annotation.ITrigger
 * @see com.oceanum.trigger.QuartzTrigger
 */
trait Trigger {
  /**
   * 启动一个触发器任务
   *
   * @param name 触发器任务名称
   * @param config 任务配置
   * @param action 当任务被触发时执行的动作，输入参数为触发时间
   * @param startTime 第一次触发时间在此之后
   */
  def start(name: String, config: Map[String, String], startTime: Option[Date])(action: Date => Unit): Unit

  /**
   * 根据任务名称停止任务
   *
   * @param name 任务名称
   * @return 是否有任务被停止，false表示没有该名称的任务，true反之
   */
  def stop(name: String): Boolean

  /**
   * 根据任务名称暂停任务
   *
   * @param name 任务名称
   * @return 是否有任务被暂停，false表示没有该名称的任务，true反之
   */
  def suspend(name: String): Boolean

  /**
   * 根据任务名称继续任务
   *
   * @param name 任务名称
   * @return 是否有任务被继续，false表示没有该名称的任务，true反之
   */
  def resume(name: String): Boolean

  /**
   * 触发器的类型名称，自定义的不同触发器类应该有不同的triggerType，系统会根据此来找到对应的触发器
   * 来启动，停止，暂停，继续任务。
   *
   * @return 触发器类型名称
   */
  def triggerType: String
}
