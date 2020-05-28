package com.oceanum.common

import java.io.File
import java.util.Properties

import com.oceanum.exec.{Executor, OperatorProp, OutputManager}
import com.oceanum.exec.multi.MultiOperatorExecutor
import com.oceanum.exec.process.ProcessExecutor

import scala.collection.JavaConversions.asScalaSet
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration
import scala.util.matching.Regex

/**
 * @author chenmingkun
 * @date 2020/5/1
 */
object Environment {

  private val properties = {
    val loader = new PropLoader
    loader.load("application-env")
    loader.load("application.properties")
    loader.get
  }


  val EXEC_PYTHON: String = getProperty("exec.python.cmd", "python3")
  val EXEC_PYTHON_ENABLED: Boolean = getProperty("exec.python.enabled", "false").toBoolean
  val EXEC_JAVA: String = getProperty("exec.java.cmd", "java")
  val EXEC_JAVA_ENABLED: Boolean = getProperty("exec.java.enabled", "false").toBoolean
  val EXEC_SCALA: String = getProperty("exec.scala.cmd", "scala")
  val EXEC_SCALA_ENABLED: Boolean = getProperty("exec.scala.enabled", "false").toBoolean
  val EXEC_SHELL: String = getProperty("exec.shell.cmd", "bash")
  val EXEC_SPARK: String = getProperty("exec.spark.cmd", "spark-submit")
  val EXEC_SPARK_ENABLED: String = getProperty("exec.spark.enabled", "false")
  val EXEC_SPARK_HOME: String = getProperty("exec.spark.home")

  val EXEC_SHELL_ENABLED: Boolean = true
  val EXEC_WORK_DIR: String = getProperty("exec.work-dir", ".")
  val EXEC_THREAD_NUM: Int = getProperty("exec.thread-num", "16").toInt
  val EXEC_MAX_TIMEOUT: Duration = Duration(getProperty("exec.max-timeout", "24h"))

  val CLUSTER_SYSTEM_NAME: String = getProperty("cluster.system-name", "cluster")
  val CLUSTER_NODE_PORT: Int = getProperty("cluster.node.port", "3551").toInt
  val CLUSTER_NODE_HOST: String = getProperty("cluster.node.host", "127.0.0.1")
  val CLUSTER_SEED_NODE: Seq[String] = getProperty("cluster.seed-node", "127.0.0.1:3551").split(",").map(node => s"akka.tcp://$CLUSTER_SYSTEM_NAME@$node").toSeq

  val CLIENT_SYSTEM_NAME: String = getProperty("client.system-name", "client")
  val CLIENT_PORT: Int = getProperty("client.port", "4551").toInt

  val DEV_MODE: Boolean = getProperty("dev-mode", "false").toBoolean
  val EXECUTORS: Array[Executor[_ <: OperatorProp]] = Array(new ProcessExecutor(OutputManager.global), new MultiOperatorExecutor())

  val SCHEDULE_EXECUTION_CONTEXT: ExecutionContext = ExecutionContext.global

  val OS: OS = {
    val sys = scala.util.Properties
    if (sys.isWin) WINDOWS
    else if (sys.isMac) MAC
    else LINUX
  }
  val PATH_SEPARATOR: String = {
    File.separator
  }
  val CLUSTER_NODE_TOPICS: Array[String] = getProperty("cluster.node.topics").split(",").map(_.trim)

  def getProperty(key: String): String = properties.getProperty(key)

  def getProperty(key: String, orElse: String): String = properties.getProperty(key, orElse)

  def toPath(uri: String): String = new File(uri).getPath

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
      properties.load(this.getClass.getClassLoader.getResourceAsStream(file))

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
