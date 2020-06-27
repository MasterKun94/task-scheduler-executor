package com.oceanum.cluster.exec

import java.io.InputStream

import com.oceanum.client.InputStreamHandler
import com.oceanum.common.Environment

/**
 * @author chenmingkun
 * @date 2020/4/29
 */
class OutputManager(num: Int) {

  private val streamOutput: MailBox[(InputStream, InputStreamHandler)] = MailBox(r => r._2.handle(r._1), num)

  def submit(input: InputStream, handler: InputStreamHandler): Unit = {
    streamOutput.send((input, handler))
  }

  def close(): Unit = {
    streamOutput.close()
  }
}

object OutputManager {
  lazy val global = new OutputManager(Environment.EXEC_THREAD_NUM * 2)
}
