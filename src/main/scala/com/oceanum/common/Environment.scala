package com.oceanum.common

import java.io.{File, FileInputStream, FileOutputStream}
import java.util.Properties

import akka.actor.ActorSystem
import com.oceanum.exec.executors.ProcessExecutor
import com.oceanum.exec.{OperatorTask, OutputManager, TypedExecutor}

import scala.collection.JavaConversions.asScalaSet
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.util.matching.Regex

/**
 * @author chenmingkun
 * @date 2020/5/1
 */
object Environment {

  private val propLoader: PropLoader = new PropLoader

  private val properties = new Properties()
  lazy val AKKA_CONF: String = parsePath(getProperty("akka.conf", s"conf${PATH_SEPARATOR}application.conf"))
  lazy val BASE_PATH: String = new File(".").getCanonicalPath
  lazy val EXEC_PYTHON: String = getProperty("exec.python.cmd", "python3")
  lazy val EXEC_PYTHON_ENABLED: Boolean = getProperty("exec.python.enabled", "false").toBoolean
  lazy val EXEC_JAVA: String = getProperty("exec.java.cmd", "java")
  lazy val EXEC_JAVA_ENABLED: Boolean = getProperty("exec.java.enabled", "false").toBoolean
  lazy val EXEC_SCALA: String = getProperty("exec.scala.cmd", "scala")
  lazy val EXEC_SCALA_ENABLED: Boolean = getProperty("exec.scala.enabled", "false").toBoolean
  lazy val EXEC_SHELL: String = getProperty("exec.shell.cmd", "bash")
  lazy val EXEC_SPARK: String = getProperty("exec.spark.cmd", "spark-submit")
  lazy val EXEC_SPARK_ENABLED: String = getProperty("exec.spark.enabled", "false")
  lazy val EXEC_SPARK_HOME: String = getProperty("exec.spark.home")

  lazy val EXEC_SHELL_ENABLED: Boolean = true
  lazy val EXEC_WORK_DIR: String = getProperty("exec.work-dir", ".")
  lazy val EXEC_THREAD_NUM: Int = getProperty("exec.thread-num", "16").toInt
  lazy val EXEC_MAX_TIMEOUT: Duration = Duration(getProperty("exec.max-timeout", "24h"))

  lazy val CLUSTER_SYSTEM_NAME: String = getProperty("cluster.system-name", "cluster")
  lazy val CLUSTER_NODE_PORT: Int = getProperty("cluster.node.port", "3551").toInt
  lazy val CLUSTER_NODE_HOST: String = getProperty("cluster.node.host", "127.0.0.1")
  lazy val CLUSTER_SEED_NODE: Seq[String] = getProperty("cluster.seed-node", "127.0.0.1:3551").split(",").map(node => s"akka.tcp://$CLUSTER_SYSTEM_NAME@$node").toSeq

  lazy val CLIENT_SYSTEM_NAME: String = getProperty("client.system-name", "client")
  lazy val CLIENT_PORT: Int = getProperty("client.port", "4551").toInt

  lazy val DEV_MODE: Boolean = getProperty("dev-mode", "false").toBoolean
  lazy val EXECUTORS: Array[TypedExecutor[_ <: OperatorTask]] = Array(new ProcessExecutor(OutputManager.global))

  lazy val SCHEDULE_EXECUTION_CONTEXT: ExecutionContext = ExecutionContext.global
  lazy val ACTOR_ALIVE_TIME_MAX: FiniteDuration = FiniteDuration(1, "d")

  lazy val OS: OS = {
    val sys = scala.util.Properties
    if (sys.isWin) WINDOWS
    else if (sys.isMac) MAC
    else LINUX
  }
  lazy val PATH_SEPARATOR: String = {
    File.separator
  }
  lazy val CLUSTER_NODE_TOPICS: Array[String] = getProperty("cluster.node.topics").split(",").map(_.trim)

  def getProperty(key: String): String = properties.getProperty(key)

  def getProperty(key: String, orElse: String): String = properties.getProperty(key, orElse)

  def toPath(uri: String): String = new File(uri).getPath

  private var sys: ActorSystem = _

  def load(files: Array[String]): Unit = {
    for (file <- files) {
      val path = parsePath(file)
      propLoader.load(path)
    }
    properties.putAll(propLoader.get)
    println(properties)
  }

  def parsePath(file: String): String = {
    if (file.startsWith("/"))
      file
    else
      BASE_PATH + PATH_SEPARATOR + file
  }

  def parse(): Unit = {

  }

  def registrySystem(actorSystem: ActorSystem): Unit = {
    sys = actorSystem
  }

  def actorSystem: ActorSystem = sys

  abstract class OS
  case object WINDOWS extends OS
  case object LINUX extends OS
  case object MAC extends OS

  private class PropLoader {
    private val properties = new Properties()
    val pattern: Regex = """(.*)\$\{(.*)}(.*)""".r  //新建一个正则表达式

    def parse(line: String, prop: Properties): String = {
      if (line == null || line.trim.isEmpty)
        ""
      else
        line match {
          case pattern(pre, reg, str) =>
            val regValue = parse(prop.getProperty(reg, System.getenv(reg)), prop)
            prop.setProperty(reg, regValue)
            if (regValue == null || regValue.trim.isEmpty) {
              throw new RuntimeException("需要在配置文件或环境变量中设置变量：" + reg)
            }
            parse(pre, prop) + regValue + parse(str, prop)
          case str: String => str
          case unknown =>
            println(unknown)
            unknown
        }
    }

    def load(file: String): Unit = {
      println("load: " + file)
      properties.load(new FileInputStream(new File(file)))

    }

    def get: Properties = {
      for (elem <- properties.keySet().map(_.toString)) {
        properties.setProperty(elem, parse(properties.getProperty(elem), properties))
      }
      properties
    }
  }

  def main(args: Array[String]): Unit = {
    println(Environment.properties)
    properties.keySet().foreach(key => println(s"$key = ${properties.get(key)}"))
    val uri: File = new File(getProperty("HADOOP_CONF_DIR"))
    println(uri.getPath)
  }
}
