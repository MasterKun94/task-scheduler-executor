package com.oceanum.file

import java.io.File
import java.nio.file.{Files, Path}

import com.oceanum.annotation.IFileSystem

import scala.concurrent.{ExecutionContext, Future}

/**
 * for test
 */
@IFileSystem
class LocalFileSystem extends FileSystem("file") {
  override def download(srcPath: String, destPath: String)(implicit ec: ExecutionContext): Future[Unit] = {
    Future {
      val dest = new File(destPath)
      if (dest.exists()) {
        dest.delete()
      }
      if (!dest.getParentFile.exists()) {
        dest.getParentFile.mkdirs()
      }
      Files.copy(new File(srcPath).toPath, dest.toPath)
    }
  }

  override def upload(srcPath: String, destPath: String)(implicit ec: ExecutionContext): Future[Unit] = {
    download(srcPath, destPath)
  }
}
