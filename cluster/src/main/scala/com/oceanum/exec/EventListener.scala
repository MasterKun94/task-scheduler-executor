package com.oceanum.exec

import com.oceanum.common.RichTaskMeta

/**
 * @author chenmingkun
 * @date 2020/4/30
 */
trait EventListener {

  def prepare(message: RichTaskMeta)

  def start(message: RichTaskMeta)

  def running(message: RichTaskMeta)

  def failed(message: RichTaskMeta)

  def success(message: RichTaskMeta)

  def retry(message: RichTaskMeta)

  def timeout(message: RichTaskMeta)

  def kill(message: RichTaskMeta)
}
