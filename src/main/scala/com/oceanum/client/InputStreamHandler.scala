package com.oceanum.client

import java.io.InputStream

/**
 * @author chenmingkun
 * @date 2020/5/4
 */
trait InputStreamHandler {
  def handle(input: InputStream)

  def close()
}
