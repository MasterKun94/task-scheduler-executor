package com.oceanum.common

import java.io.File
import java.util.regex.Matcher

import com.oceanum.client.RichTaskMeta
import com.oceanum.expr.JavaMap
import com.oceanum.graph.RichGraphMeta

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

  implicit class EnvHelper(env: Map[String, Any]) {
    def combineGraph(graphMeta: RichGraphMeta): Map[String, Any] = graphMeta.env ++ env + (EnvHelper.graphKey -> graphMeta)

    def getGraph: RichGraphMeta = env(EnvHelper.graphKey).asInstanceOf[RichGraphMeta]

    def addTask(taskMeta: RichTaskMeta): Map[String, Any] = env + (EnvHelper.taskKey -> taskMeta)

    def getTask: RichTaskMeta = env.get(EnvHelper.taskKey).map(_.asInstanceOf[RichTaskMeta]).getOrElse(RichTaskMeta.empty)

    def toJava: JavaMap[String, AnyRef] = evaluate(toJava(env).asInstanceOf[JavaMap[String, AnyRef]])

    private def toJava(ref: Any): AnyRef = ref.asInstanceOf[AnyRef] match {
      case map: Map[_, _] => map.mapValues(_.asInstanceOf[AnyRef]).mapValues(toJava).asJava
      case seq: Seq[_] => seq.map(_.asInstanceOf[AnyRef]).map(toJava).asJava
      case set: Set[_] => set.map(_.asInstanceOf[AnyRef]).map(toJava).asJava
      case itr: Iterable[_] => itr.map(_.asInstanceOf[AnyRef]).map(toJava).asJava
      case itr: Iterator[_] => itr.map(_.asInstanceOf[AnyRef]).map(toJava).asJava
      case default => default
    }

    def evaluate(ref: AnyRef, env: JavaMap[String, AnyRef]): AnyRef = ref match {
      case str: String => StringParser.parseExprRaw(str)(env)
      case map: java.util.Map[_, _] => map.asScala.mapValues(v => evaluate(v.asInstanceOf[AnyRef], env)).asJava
      case seq: java.util.List[_] => seq.asScala.map(v => evaluate(v.asInstanceOf[AnyRef], env)).asJava
      case set: java.util.Set[_] => set.asScala.map(v => evaluate(v.asInstanceOf[AnyRef], env)).asJava
      case default => default
    }

    def evaluate(env: JavaMap[String, AnyRef]): JavaMap[String, AnyRef] = {
      evaluate(env, env).asInstanceOf[JavaMap[String, AnyRef]]
    }
  }

  object EnvHelper {
    val taskKey = "task"
    val graphKey = "graph"
  }
}
