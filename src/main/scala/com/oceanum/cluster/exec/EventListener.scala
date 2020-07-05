package com.oceanum.cluster.exec

import com.oceanum.client.Metadata

/**
 * @author chenmingkun
 * @date 2020/4/30
 */
trait EventListener {

  def prepare(message: Metadata = Metadata.empty)

  def start(message: Metadata = Metadata.empty)

  def running(message: Metadata = Metadata.empty)

  def failed(message: Metadata = Metadata.empty)

  def success(message: Metadata = Metadata.empty)

  def retry(message: Metadata = Metadata.empty)

  def timeout(message: Metadata = Metadata.empty)

  def kill(message: Metadata = Metadata.empty)
}
