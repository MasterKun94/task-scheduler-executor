package com.oceanum.common

import java.io.File
import java.util.regex.Matcher

import com.oceanum.client.{Metadata, Task}
import com.oceanum.cluster.exec.{InputStreamHandler, LineHandler}

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.util.Properties

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

  implicit class TaskMetadataHelper(metadata: Metadata) {

    lazy val id: String = metadata("id")
    lazy val taskType: String = metadata("taskType")
    lazy val user: String = metadata("user")
    lazy val createTime: String = metadata("createTime")
    lazy val stdoutHandler: InputStreamHandler = LineHandler.fileOutputHandler((outputPath/s"$id-stdout.out").toFile)
    lazy val stderrHandler: InputStreamHandler = LineHandler.fileOutputHandler((outputPath/s"$id-stderr.out").toFile)

    private lazy val  outputPath: String = {
      //创建文件路径//创建文件路径
      val file: File = (Environment.BASE_PATH / "app-output").toFile
      //判断文件父目录是否已经存在,不存在则创建
      if (!file.exists)
        file.mkdirs
      file.getAbsolutePath
    }

    def withTask(task: Task): Metadata = {
      metadata ++ Metadata(
        "id" -> task.id,
        "taskType" -> task.prop.taskType,
        "user" -> task.user,
        "createTime" -> System.currentTimeMillis().toString
      )
    }
  }

  implicit class PathHelper(str: String) {

    def / (subStr: String): String = {
      val sep = Environment.PATH_SEPARATOR
      val path = toPath(str, sep)
      val subPath = toPath(subStr, sep)
      if (path.endsWith(sep))
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

    def / : String = this / ""

    def toFile : File = new File(str)

    def toAbsolute(separator: String = Environment.PATH_SEPARATOR): String = {
      val p = if (new File(str).isAbsolute) str else Environment.BASE_PATH / str
      toPath(p, separator)
    }

    def toPath(separator: String = Environment.PATH_SEPARATOR): String = {
      toPath(str, separator)
    }

    private def toPath(str: String, separator: String): String = {
      str.replaceAll("[/\\\\]", Matcher.quoteReplacement(separator))
    }
  }

  def main(args: Array[String]): Unit = {
    println(Properties.javaHome)
    println(Properties.javaHome.toAbsolute(", "))
    println("C:" / "/tmp/" / "hello/" / "/test" / "123123" / "aaaa")
  }
}
