package com.oceanum.exec.process

import java.io.IOException

import com.oceanum.exec.{LineHandler, OperatorProp}

class ProcessProp(val propCmd: Array[String] = Array.empty,
                  val propEnv: Map[String, String] = Map.empty,
                  val propDirectory: String = "",
                  val propWaitForTimeout: Long = -1,
                  val propStdoutHandler: LineHandler,
                  val propStderrHandler: LineHandler) extends OperatorProp {
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