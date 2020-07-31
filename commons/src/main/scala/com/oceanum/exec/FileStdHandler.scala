package com.oceanum.exec

import java.io.{BufferedWriter, FileOutputStream, OutputStreamWriter}
import java.nio.charset.StandardCharsets

import scala.util.Properties

class FileStdHandler(stdoutPath: String) extends LineStdHandler {
  val writer: BufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(stdoutPath), StandardCharsets.UTF_8))

  override def handle(line: String): Unit = {
    val realLine = if (Properties.isWin) {
      line.replace("\\u", "\\\\u")
    } else {
      line
    }
    writer.write(realLine)
    writer.newLine()
  }

  override def close(): Unit = {
    writer.flush()
    writer.close()
  }

  override def before(): Unit = {
    writer.newLine()
    writer.write("+++++++++++++ START +++++++++++++")
    writer.newLine()
  }

  override def after(): Unit = {
    writer.newLine()
    writer.write("++++++++++++++ END ++++++++++++++")
    writer.newLine()
  }
}
