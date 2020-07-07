package com.oceanum.common

import java.io.File
import java.text.SimpleDateFormat
import java.util.regex.Matcher

import com.oceanum.client.{Metadata, Task}
import com.oceanum.cluster.exec.InputStreamHandler

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

    def id: String = metadata("id")
    def taskType: String = metadata("taskType")
    def user: String = metadata("user")
    def createTime: String = metadata("createTime")
    def stdoutHandler: InputStreamHandler = Class.forName(Environment.CLUSTER_NODE_RUNNER_STDOUT_HANDLER)
      .getConstructor(metadata.getClass)
      .newInstance(metadata)
      .asInstanceOf[InputStreamHandler]
    def stderrHandler: InputStreamHandler = Class.forName(Environment.CLUSTER_NODE_RUNNER_STDERR_HANDLER)
      .getConstructor(metadata.getClass)
      .newInstance(metadata)
      .asInstanceOf[InputStreamHandler]
    def execDir: String = metadata("execDir")

    type BuilderFunc = Task => Task
    def setLazyInit(func: BuilderFunc): Metadata = metadata + ("lazyInit" -> func)

    def lazyInit(task: Task): Task = metadata.get("lazyInit") match {
      case Some(f) => f.asInstanceOf[BuilderFunc](task)
      case None => task
    }

    private lazy val outputPath: String = {
      //创建文件路径//创建文件路径
      val file: File = (execDir/"out").toFile
      //判断文件父目录是否已经存在,不存在则创建
      if (!file.exists)
        file.mkdirs
      file.getAbsolutePath
    }
    def stdoutPath: String = outputPath/s"$id-stdout.out"
    def stderrPath: String = outputPath/s"$id-stderr.out"

    def withTask(task: Task): Metadata = {
      val dateFormat = new SimpleDateFormat("yyyyMMdd").format(System.currentTimeMillis())
      metadata ++ Metadata(
        "id" -> task.id,
        "taskType" -> task.prop.taskType,
        "user" -> task.user,
        "createTime" -> System.currentTimeMillis().toString,
        "execDir" -> Environment.EXEC_WORK_DIR/dateFormat/task.user/task.id
      )
    }
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

    def / : String = this / ""

    def toFile : File = new File(str)

    def toAbsolute(sep: String = separator): String = {
      val p = if (new File(str).isAbsolute) str else Environment.BASE_PATH / str
      toPath(p, sep)
    }

    def toPath(sep: String = separator): String = {
      toPath(str, sep)
    }

    private def toPath(str: String, separator: String): String = {
      str.replaceAll("[/\\\\]", Matcher.quoteReplacement(separator))
    }
  }

  def main(args: Array[String]): Unit = {
    println(Properties.javaHome)
    implicit val sep: String = "/"
    println(Properties.javaHome.toAbsolute(", "))
    println("C:" / "/tmp/" / "hello/" / "/test" / "123123" / "aaaa" / )
  }
}
