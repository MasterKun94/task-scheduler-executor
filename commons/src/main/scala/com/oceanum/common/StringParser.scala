package com.oceanum.common

import scala.util.matching.Regex

abstract class StringParser[T] {
  private val pattern: Regex = """(.*)\$\{(.*)}(.*)""".r  //新建一个正则表达式

  def parse(line: String)(implicit env: T): String = {
    if (line == null || line.trim.isEmpty)
      ""
    else
      line match {
        case pattern(pre, reg, str) =>
          parse(pre) + replace(reg) + parse(str)
        case str: String => str
        case unknown =>
          println(unknown)
          unknown
      }
  }

  protected def replace(regex: String)(implicit env: T): String
}
