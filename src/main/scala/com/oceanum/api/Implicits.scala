package com.oceanum.api

import java.io.File

import com.oceanum.common.Environment

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

  implicit class MetadataHelper(metadata: Map[String, String]) {
    private def outputPath = {
      val path = Environment.BASE_PATH + Environment.PATH_SEPARATOR + "app-output" + Environment.PATH_SEPARATOR
      //创建文件路径//创建文件路径
      val dest = new File(path)
      //判断文件父目录是否已经存在,不存在则创建
      if (!dest.exists) dest.mkdir
      path
    }

    def stdoutHandler: InputStreamHandler = LineHandler.fileOutputHandler(new File(outputPath + metadata("appName") + "-stdout.out"))

    def stderrHandler: InputStreamHandler = LineHandler.fileOutputHandler(new File(outputPath + metadata("appName") + "-stderr.out"))

    def withTask(task: Task): Map[String, String] = {
      val addition = Map(
        "appName" -> task.name,
        "taskType" -> task.prop.taskType
      )
      metadata ++ addition
    }
  }
}
