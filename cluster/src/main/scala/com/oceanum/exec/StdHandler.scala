package com.oceanum.exec

import java.io.InputStream

/**
 * @author chenmingkun
 * @date 2020/5/4
 */
trait StdHandler {
  def handle(input: InputStream)

  def close()
}

object StdHandler {
  def empty: StdHandler = new StdHandler {
    override def handle(input: InputStream): Unit = {}

    override def close(): Unit = {}
  }
}
