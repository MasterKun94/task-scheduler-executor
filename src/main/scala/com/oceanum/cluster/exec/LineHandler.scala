package com.oceanum.cluster.exec

import java.io._
import java.nio.charset.StandardCharsets

import com.oceanum.client.Metadata

import scala.util.Properties

/**
 * @author chenmingkun
 * @date 2020/4/29
 */
trait LineHandler extends InputStreamHandler {
  override def handle(input: InputStream): Unit = {
    val reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))
    try {
      before()
      Stream.continually(reader.readLine())
        .foreach(line => {
          if (line == null) {
            return
          }
          handle(line)
        })
      after()
    } catch {
      case e: IOException =>
        throw new RuntimeException("Stdout/Stderr read/write error", e)
    } finally {
      try {
        reader.close()
      } catch {
        case ex: IOException =>
          ex.printStackTrace()
      }
    }
  }

  def handle(line: String)

  def before(): Unit = {}

  def after(): Unit = {}
}

class FileOutputHandler(stdoutPath: String) extends LineHandler {
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

import com.oceanum.common.Implicits.TaskMetadataHelper
class StdoutFileOutputHandler(metadata: Metadata) extends FileOutputHandler(metadata.stdoutPath) {}
class StderrFileOutputHandler(metadata: Metadata) extends FileOutputHandler(metadata.stderrPath) {}