package com.oceanum.exec

import com.oceanum.common.{SystemInit, TaskMeta}

trait StdHandlerFactory {
  def createStdOutHandler(taskMeta: TaskMeta): StdHandler

  def createStdErrHandler(taskMeta: TaskMeta): StdHandler
}

object StdHandlerFactory {
  lazy val default: StdHandlerFactory = SystemInit.stdHandlerFactory
}
