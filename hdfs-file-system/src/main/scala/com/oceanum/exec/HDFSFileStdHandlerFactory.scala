package com.oceanum.exec
import com.oceanum.annotation.IStdHandlerFactory
import com.oceanum.common.Implicits.PathHelper
import com.oceanum.common.{Environment, TaskMeta}

@IStdHandlerFactory(priority = -10)
class HDFSFileStdHandlerFactory extends StdHandlerFactory {
  private val baseDir = Environment.getProperty("hdfs.base-dir", "/tmp")

  override def createStdOutHandler(taskMeta: TaskMeta): StdHandler = {
    new HDFSStdHandler(baseDir/taskMeta.execDir/"stdout.out")
  }

  override def createStdErrHandler(taskMeta: TaskMeta): StdHandler = {
    new HDFSStdHandler(baseDir/taskMeta.execDir/"stderr.out")
  }
}
