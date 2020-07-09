package com.oceanum.cluster.exec

import com.oceanum.client.TaskMeta

/**
 * @author chenmingkun
 * @date 2020/4/30
 */
trait EventListener {

  def prepare(message: TaskMeta = TaskMeta.empty)

  def start(message: TaskMeta = TaskMeta.empty)

  def running(message: TaskMeta = TaskMeta.empty)

  def failed(message: TaskMeta = TaskMeta.empty)

  def success(message: TaskMeta = TaskMeta.empty)

  def retry(message: TaskMeta = TaskMeta.empty)

  def timeout(message: TaskMeta = TaskMeta.empty)

  def kill(message: TaskMeta = TaskMeta.empty)
}
