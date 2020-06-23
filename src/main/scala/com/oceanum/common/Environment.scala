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

  lazy val AKKA_CONF: String = parsePath(getProperty(Key.AKKA_CONF, s"conf${PATH_SEPARATOR}application.conf"))
  lazy val BASE_PATH: String = getProperty(Key.BASE_PATH)
  lazy val EXEC_PYTHON: String = getProperty(Key.EXEC_PYTHON, "python3")
  lazy val EXEC_PYTHON_ENABLED: Boolean = getProperty(Key.EXEC_PYTHON_ENABLED, "false").toBoolean
  lazy val EXEC_JAVA: String = getProperty(Key.EXEC_JAVA, "java")
  lazy val EXEC_JAVA_ENABLED: Boolean = getProperty(Key.EXEC_JAVA_ENABLED, "false").toBoolean
  lazy val EXEC_SCALA: String = getProperty(Key.EXEC_SCALA, "scala")
  lazy val EXEC_SCALA_ENABLED: Boolean = getProperty(Key.EXEC_SCALA_ENABLED, "false").toBoolean
  lazy val EXEC_SHELL: String = getProperty(Key.EXEC_SHELL, "bash")
  lazy val EXEC_SPARK: String = getProperty(Key.EXEC_SPARK, "spark-submit")
  lazy val EXEC_SPARK_ENABLED: String = getProperty(Key.EXEC_SPARK_ENABLED, "false")
  lazy val EXEC_SPARK_HOME: String = getProperty(Key.EXEC_SPARK_HOME)

  lazy val EXEC_SHELL_ENABLED: Boolean = getProperty(Key.EXEC_SHELL_ENABLED, "true").toBoolean
  lazy val EXEC_WORK_DIR: String = getProperty(Key.EXEC_WORK_DIR, ".")
  lazy val EXEC_THREAD_NUM: Int = getProperty(Key.EXEC_THREAD_NUM, "16").toInt
  lazy val EXEC_MAX_TIMEOUT: Duration = Duration(getProperty(Key.EXEC_MAX_TIMEOUT, "24h"))

  lazy val CLUSTER_SYSTEM_NAME: String = getProperty(Key.CLUSTER_SYSTEM_NAME, "cluster")
  lazy val CLUSTER_NODE_PORT: Int = getProperty(Key.CLUSTER_NODE_PORT, "3551").toInt
  lazy val CLUSTER_NODE_HOST: String = getProperty(Key.CLUSTER_NODE_HOST, "127.0.0.1")
  lazy val CLUSTER_SEED_NODE: Seq[String] = getProperty(Key.CLUSTER_SEED_NODE, "127.0.0.1:3551").split(",").map(node => s"akka.tcp://$CLUSTER_SYSTEM_NAME@$node").toSeq

  lazy val CLIENT_SYSTEM_NAME: String = getProperty(Key.CLIENT_SYSTEM_NAME, "client")
  lazy val CLIENT_PORT: Int = getProperty(Key.CLIENT_PORT, "4551").toInt
  lazy val CLIENT_HOST: String = getProperty(Key.CLIENT_HOST, "127.0.0.1")

  lazy val DEV_MODE: Boolean = getProperty(Key.DEV_MODE, "false").toBoolean

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
  lazy val CLUSTER_NODE_TOPICS: Seq[String] = getProperty("cluster.node.topics").split(",").map(_.trim).toSeq

  def print(): Unit = {
    import scala.collection.JavaConversions.mapAsScalaMap
    println("config:")
    properties.foreach(kv => println(s"\t${kv._1} -> ${kv._2}"))
  }

  def getProperty(key: String): String = properties.getProperty(key)

  def getProperty(key: String, orElse: String): String = properties.getProperty(key, orElse)

  def toPath(uri: String): String = new File(uri).getPath

  private var sys: ActorSystem = _

  def load(files: Array[String]): Unit = {
    println("load:")
    for (file <- files) {
      val path = parsePath(file)
      println("\t" + path)
      propLoader.load(path)
    }
    properties.putAll(propLoader.get)
  }

  def load(key: String, value: String): Unit = {
    properties.put(key, value)
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
      properties.load(new FileInputStream(new File(file)))
    }

    def get: Properties = {
      for (elem <- properties.keySet().map(_.toString)) {
        properties.setProperty(elem, parse(properties.getProperty(elem), properties))
      }
      properties
    }
  }

  object Key {
    val AKKA_CONF: String = "akka.conf"
    val BASE_PATH: String = "base.path"
    val EXEC_PYTHON: String = "exec.python.cmd"
    val EXEC_PYTHON_ENABLED: String = "exec.python.enabled"
    val EXEC_JAVA: String = "exec.java.cmd"
    val EXEC_JAVA_ENABLED: String = "exec.java.enabled"
    val EXEC_SCALA: String = "exec.scala.cmd"
    val EXEC_SCALA_ENABLED: String = "exec.scala.enabled"
    val EXEC_SHELL: String = "exec.shell.cmd"
    val EXEC_SHELL_ENABLED: String = "exec.shell.enabled"
    val EXEC_SPARK: String = "exec.spark.cmd"
    val EXEC_SPARK_ENABLED: String = "exec.spark.enabled"
    val EXEC_SPARK_HOME: String = "exec.spark.home"

    val EXEC_WORK_DIR: String = "exec.work-dir"
    val EXEC_THREAD_NUM: String = "exec.thread-num"
    val EXEC_MAX_TIMEOUT: String = "exec.max-timeout"

    val CLUSTER_SYSTEM_NAME: String = "cluster.system-name"
    val CLUSTER_NODE_PORT: String = "cluster.node.port"
    val CLUSTER_NODE_HOST: String = "cluster.node.host"
    val CLUSTER_SEED_NODE: String = "cluster.seed-node"

    val CLIENT_SYSTEM_NAME: String = "client.system-name"
    val CLIENT_PORT: String = "client.port"
    val CLIENT_HOST: String = "client.host"

    val DEV_MODE: String = "dev-mode"
    val CLUSTER_NODE_TOPICS: String = "cluster.node.topics"
  }

  def main(args: Array[String]): Unit = {
    println(Environment.properties)
    properties.keySet().foreach(key => println(s"$key = ${properties.get(key)}"))
    val uri: File = new File(getProperty("HADOOP_CONF_DIR"))
    println(uri.getPath)
  }
}
