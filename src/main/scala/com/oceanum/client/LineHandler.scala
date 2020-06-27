package com.oceanum.client

import java.io._
import java.nio.charset.StandardCharsets

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

object LineHandler {
  def fileOutputHandler(file: File): LineHandler = {
    new LineHandler {
      val writer: BufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))

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
  }

  def printHandler(name: String): LineHandler = {
    new LineHandler {
      override def handle(line: String): Unit = println(s"$name OUTPUT: " + line)

      override def close(): Unit = {}

      override def before(): Unit = println(s"+++++++++++++ START $name +++++++++++++")

      override def after(): Unit = println(s"++++++++++++++ END $name ++++++++++++++")
    }
  }

  def multiHandler(array: LineHandler*): LineHandler = {
    new LineHandler {
      override def handle(line: String): Unit = array.foreach(_.handle(line))

      override def close(): Unit = array.foreach(_.close())

      override def before(): Unit = array.foreach(_.before())

      override def after(): Unit = array.foreach(_.after())
    }
  }
}
