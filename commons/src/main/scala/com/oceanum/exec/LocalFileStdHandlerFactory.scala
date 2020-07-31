package com.oceanum.exec
import java.io.File

import com.oceanum.annotation.IStdHandlerFactory
import com.oceanum.common.Implicits.PathHelper
import com.oceanum.common.TaskMeta

@IStdHandlerFactory(priority = -1)
class LocalFileStdHandlerFactory extends StdHandlerFactory {

  override def createStdOutHandler(taskMeta: TaskMeta): StdHandler = {
    new FileStdHandler(outputPath(taskMeta)/"stdout.out")
  }

  override def createStdErrHandler(taskMeta: TaskMeta): StdHandler = {
    new FileStdHandler(outputPath(taskMeta)/"stderr.out")
  }

  private def outputPath(taskMeta: TaskMeta): String = {
    //创建文件路径//创建文件路径
    val file: File = (taskMeta.execDir/"out").toFile
    //判断文件父目录是否已经存在,不存在则创建
    if (!file.exists)
      file.mkdirs
    file.getAbsolutePath
  }
}
