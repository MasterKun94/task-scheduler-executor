package com.oceanum.exec

import java.io._
import java.nio.charset.StandardCharsets
import java.util.function.Consumer

import scala.util.Properties

/**
 * @author chenmingkun
 * @date 2020/4/29
 */
trait LineStdHandler extends StdHandler {
  override def handle(input: InputStream): Unit = {
    val reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))
    try {
      before()
      reader
        .lines()
        .forEachOrdered(new Consumer[String]() {
          override def accept(line: String): Unit = {
            if (line == null) {
              return
            }
            handle(line)
          }
        })
      after()
    } catch {
      case e: IOException =>
        throw new RuntimeException("Stdout/Stderr read/write error", e)
      case e: Throwable =>
        e.printStackTrace()
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