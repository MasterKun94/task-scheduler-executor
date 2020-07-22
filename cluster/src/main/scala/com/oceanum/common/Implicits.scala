package com.oceanum.common

import java.io.File
import java.util.regex.Matcher

import com.oceanum.exec.RichGraphMeta
import com.oceanum.expr.JavaMap

import scala.collection.JavaConverters._
import scala.concurrent.duration.{Duration, FiniteDuration}

/**
 * @author chenmingkun
 * @date 2020/6/22
 */
object Implicits {
  implicit class DurationHelper(val sc: StringContext) extends AnyVal {
    def d(args: Any*): Duration = toDuration(args)

    def fd(args: Any*): FiniteDuration = {
      val duration = toDuration(args)
      FiniteDuration(duration._1, duration._2)
    }

    def duration(args: Any*): Duration = toDuration(args)

    def finiteDuration(args: Any*): FiniteDuration = {
      val duration = toDuration(args)
      FiniteDuration(duration._1, duration._2)
    }

    private def toDuration(args: Seq[Any]): Duration = Duration(sc.s(args: _*))
  }

  implicit class PathHelper(str: String)(implicit separator: String = Environment.PATH_SEPARATOR) {

    def / (subStr: String): String = {
      val sep = separator
      val path = toPath(str, sep)
      val subPath = toPath(subStr, sep)
      if (path.endsWith(sep) && !path.endsWith(":" + sep + sep))
        if (subPath.startsWith(sep))
          path + subPath.substring(1, subPath.length)
        else
          path + subPath
      else
        if (subPath.startsWith(sep))
          path + subPath
        else
          path + sep + subPath
    }

    def / (sub: AnyVal): String = this / sub.toString

    def / : String = this / ""

    def toFile : File = new File(str)

    def toAbsolute(sep: String = separator): String = {
      val p = if (new File(str).isAbsolute) str else Environment.BASE_PATH / str
      toPath(p, sep)
    }

    def toPath(sep: String = separator): String = {
      if (str.trim.equals("")) {
        throw new IllegalArgumentException("路径为空")
      }
      toPath(str, sep)
    }

    private def toPath(str: String, separator: String): String = {
      str.replaceAll("[/\\\\]", Matcher.quoteReplacement(separator))
    }
  }
}
