package com.oceanum.client

import java.io.File

import com.oceanum.exec.LineHandler

/**
 * @author chenmingkun
 * @date 2020/5/2
 */
trait LineHandlerProducer extends Serializable {
  def produce: LineHandler
}

object LineHandlerProducer {
  def file(path: String): LineHandlerProducer = {
    new LineHandlerProducer {
      override def produce: LineHandler = LineHandler.fileOutputHandler(new File(path))
    }
  }

  def print(): LineHandlerProducer = {
    new LineHandlerProducer {
      override def produce: LineHandler = LineHandler.printHandler("print handler")
    }
  }
}
