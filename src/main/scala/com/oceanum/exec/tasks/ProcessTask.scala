package com.oceanum.exec.tasks

import java.io.IOException

import com.oceanum.api.InputStreamHandler
import com.oceanum.exec.OperatorTask

class ProcessTask(val propCmd: Array[String] = Array.empty,
                  val propEnv: Map[String, String] = Map.empty,
                  val propDirectory: String = "",
                  val propWaitForTimeout: Long = -1,
                  val propStdoutHandler: InputStreamHandler,
                  val propStderrHandler: InputStreamHandler) extends OperatorTask {
  override def close(): Unit = {
    try {
      propStderrHandler.close()
      propStdoutHandler.close()
    } catch {
      case e: IOException =>
        throw new RuntimeException("Unable to close the handler :" + e)
    }
  }
}