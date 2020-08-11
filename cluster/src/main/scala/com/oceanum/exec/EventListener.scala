package com.oceanum.exec

import com.oceanum.common.RichTaskMeta

/**
 * @author chenmingkun
 * @date 2020/4/30
 */
trait EventListener {

  def prepare(message: RichTaskMeta => RichTaskMeta)

  def start(message: RichTaskMeta => RichTaskMeta)

  def running(message: RichTaskMeta => RichTaskMeta)

  def failed(message: RichTaskMeta => RichTaskMeta)

  def success(message: RichTaskMeta => RichTaskMeta)

  def retry(message: RichTaskMeta => RichTaskMeta)

  def timeout(message: RichTaskMeta => RichTaskMeta)

  def kill(message: RichTaskMeta => RichTaskMeta)
}
