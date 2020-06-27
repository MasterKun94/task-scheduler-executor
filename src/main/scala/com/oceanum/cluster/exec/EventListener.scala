package com.oceanum.cluster.exec

/**
 * @author chenmingkun
 * @date 2020/4/30
 */
trait EventListener {
  def prepare(message: Any = "")

  def start(message: Any = "")

  def running(message: Any = "")

  def failed(message: Any = "")

  def success(message: Any = "")

  def retry(message: Any = "")

  def timeout(message: Any = "")

  def kill(message: Any = "")
}
