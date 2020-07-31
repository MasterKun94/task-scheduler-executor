package com.oceanum.exec
import java.io.InputStream

import com.oceanum.common.Environment
import com.oceanum.file.HDFSFileSystem
import org.apache.hadoop.io.IOUtils

class HDFSStdHandler(path: String) extends StdHandler {
  private lazy val uploadFileStream = HDFSFileSystem.uploadFileStream(path)

  override def handle(input: InputStream): Unit = {
    IOUtils.copyBytes(input, uploadFileStream, Environment.HADOOP_BUFFER_SIZE, false)
    input.close()
  }

  override def close(): Unit = {
    uploadFileStream.flush()
    uploadFileStream.close()
  }
}
