package com.oceanum.cluster.exec

import java.io.InputStream

/**
 * @author chenmingkun
 * @date 2020/5/4
 */
trait InputStreamHandler {
  def handle(input: InputStream)

  def close()
}

object InputStreamHandler {
  def empty: InputStreamHandler = new InputStreamHandler {
    override def handle(input: InputStream): Unit = {}

    override def close(): Unit = {}
  }
}
