package com.oceanum.cluster.exec

import com.oceanum.client.RichTaskMeta

/**
 * @author chenmingkun
 * @date 2020/4/30
 */
trait EventListener {

  def prepare(message: RichTaskMeta = RichTaskMeta.empty)

  def start(message: RichTaskMeta = RichTaskMeta.empty)

  def running(message: RichTaskMeta = RichTaskMeta.empty)

  def failed(message: RichTaskMeta = RichTaskMeta.empty)

  def success(message: RichTaskMeta = RichTaskMeta.empty)

  def retry(message: RichTaskMeta = RichTaskMeta.empty)

  def timeout(message: RichTaskMeta = RichTaskMeta.empty)

  def kill(message: RichTaskMeta = RichTaskMeta.empty)
}
