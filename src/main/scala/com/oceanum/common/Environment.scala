package com.oceanum.common

import java.io.{File, FileInputStream}
import java.util.Properties

import akka.actor.ActorSystem
import com.oceanum.client.Implicits.DurationHelper
import com.oceanum.cluster.exec.{OperatorTask, OutputManager, TypedRunner}
import com.oceanum.cluster.executors.ProcessRunner
import com.oceanum.client.Implicits.PathHelper
import com.typesafe.config.ConfigFactory

import scala.collection.JavaConversions.seqAsJavaList
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.util.matching.Regex
import scala.util.{Properties => SystemProp}

/**
 * @author chenmingkun
 * @date 2020/5/1
 */
object Environment {

  private val properties = new Properties()

  lazy val AKKA_CONF: String = parsePath(getProperty(Key.AKKA_CONF, "conf" / "application.conf"))
  lazy val BASE_PATH: String = getProperty(Key.BASE_PATH).toPath()
  lazy val EXEC_PYTHON: String = getProperty(Key.EXEC_PYTHON, "python3")
  lazy val EXEC_PYTHON_ENABLED: Boolean = getProperty(Key.EXEC_PYTHON_ENABLED, "false").toBoolean
  lazy val EXEC_JAVA: String = getProperty(Key.EXEC_JAVA, SystemProp.javaHome / "path" / "java")
  lazy val EXEC_JAVA_ENABLED: Boolean = getProperty(Key.EXEC_JAVA_ENABLED, "false").toBoolean
  lazy val EXEC_SCALA: String = getProperty(Key.EXEC_SCALA, "scala")
  lazy val EXEC_SCALA_ENABLED: Boolean = getProperty(Key.EXEC_SCALA_ENABLED, "false").toBoolean
  lazy val EXEC_SHELL: String = getProperty(Key.EXEC_SHELL, "bash")
  lazy val EXEC_SPARK: String = getProperty(Key.EXEC_SPARK, "spark-submit")
  lazy val EXEC_SPARK_ENABLED: String = getProperty(Key.EXEC_SPARK_ENABLED, "false")
  lazy val EXEC_SPARK_HOME: String = getProperty(Key.EXEC_SPARK_HOME)

  lazy val EXEC_SHELL_ENABLED: Boolean = getProperty(Key.EXEC_SHELL_ENABLED, "true").toBoolean
  lazy val EXEC_WORK_DIR: String = getProperty(Key.EXEC_WORK_DIR, "")
  lazy val EXEC_THREAD_NUM: Int = getProperty(Key.EXEC_THREAD_NUM, "16").toInt
  lazy val EXEC_MAX_TIMEOUT: FiniteDuration = fd"${getProperty(Key.EXEC_MAX_TIMEOUT, "24h")}"

  lazy val HOST: String = getProperty(Key.HOST, "127.0.0.1")
  lazy val CLUSTER_NODE_SYSTEM_NAME: String = getProperty(Key.CLUSTER_NODE_SYSTEM_NAME, "cluster")
  lazy val CLUSTER_NODE_PORT: Int = getProperty(Key.CLUSTER_NODE_PORT, "3551").toInt
  lazy val CLUSTER_NODE_SEEDS: Seq[String] = getProperty(Key.CLUSTER_NODE_SEEDS, "127.0.0.1:3551").split(",").map(node => s"akka.tcp://$CLUSTER_NODE_SYSTEM_NAME@$node").toSeq
  lazy val CLUSTER_NODE_TOPICS: Seq[String] = getProperty(Key.CLUSTER_NODE_TOPICS).split(",").map(_.trim).toSeq
  lazy val CLUSTER_NODE_SYSTEM: ActorSystem = clusterSystem()

  lazy val CLIENT_NODE_SYSTEM_NAME: String = getProperty(Key.CLIENT_NODE_SYSTEM_NAME, "client")
  lazy val CLIENT_NODE_PORT: Int = getProperty(Key.CLIENT_NODE_PORT, "4551").toInt
  lazy val CLIENT_NODE_LOGGER: String = logger
  lazy val CLIENT_SYSTEM: ActorSystem = clientSystem()
  lazy val CLUSTER_NODE_METRICS_SAMPLE_INTERVAL: String = getProperty(Key.CLUSTER_NODE_METRICS_SAMPLE_INTERVAL, "5s")
  lazy val CLUSTER_NODE_METRICS_TOPIC: String = "cluster-node-metrics"
  lazy val CLUSTER_NODE_LOGGER: String = logger

  lazy val REGISTRY_NODE_SYSTEM_NAME: String = getProperty(Key.REGISTRY_NODE_SYSTEM_NAME, "registry")
  lazy val REGISTRY_NODE_PORT: Int = getProperty(Key.REGISTRY_NODE_PORT, "4551").toInt

  lazy val FILE_SERVER_SYSTEM: ActorSystem = fileServerSystem()
  lazy val FILE_SERVER_CONTEXT_PATH: String = getProperty(Key.FILE_SERVER_CONTEXT_PATH, "file")
  lazy val FILE_SERVER_PORT: Int = getProperty(Key.FILE_SERVER_PORT, "8011").toInt
  lazy val FILE_SERVER_SYSTEM_NAME: String = getProperty(Key.FILE_SERVER_SYSTEM_NAME, "file-server")
  lazy val FILE_SERVER_CHUNK_SIZE: Int = getProperty(Key.FILE_SERVER_CHUNK_SIZE, "8192").toInt
  lazy val FILE_SERVER_BASE_PATH: String = getProperty(Key.FILE_SERVER_BASE_PATH, if (OS == WINDOWS) "" else "/")
  lazy val FILE_SERVER_RECURSIVE_TRANSFER_MAX: Int = getProperty(Key.FILE_SERVER_RECURSIVE_TRANSFER_MAX, "100").toInt
  lazy val FILE_SERVER_DISPATCHER_CORE_POOL_SIZE_MIN: Int = getProperty(Key.FILE_SERVER_DISPATCHER_CORE_POOL_SIZE_MIN, "6").toInt
  lazy val FILE_SERVER_DISPATCHER_CORE_POOL_SIZE_FACTOR: Int = getProperty(Key.FILE_SERVER_DISPATCHER_CORE_POOL_SIZE_MIN, "5").toInt
  lazy val FILE_SERVER_DISPATCHER_CORE_POOL_SIZE_MAX: Int = getProperty(Key.FILE_SERVER_DISPATCHER_CORE_POOL_SIZE_MAX, "60").toInt
  lazy val FILE_SERVER_HOST_CONNECTION_POOL_MAX_CONNECTIONS: Int = getProperty(Key.FILE_SERVER_HOST_CONNECTION_POOL_MAX_CONNECTIONS, "12").toInt
  lazy val FILE_SERVER_HOST_CONNECTION_POOL_MIN_CONNECTIONS: Int = getProperty(Key.FILE_SERVER_HOST_CONNECTION_POOL_MIN_CONNECTIONS, "1").toInt
  lazy val FILE_SERVER_HOST_CONNECTION_POOL_MAX_RETRIES: Int = getProperty(Key.FILE_SERVER_HOST_CONNECTION_POOL_MAX_RETRIES, "5").toInt
  lazy val FILE_SERVER_HOST_CONNECTION_POOL_MAX_OPEN_REQUESTS: Int = getProperty(Key.FILE_SERVER_HOST_CONNECTION_POOL_MAX_OPEN_REQUESTS, "64").toInt
  lazy val FILE_SERVER_LOGGER: String = logger

  lazy val TASK_RUNNERS: Array[TypedRunner[_ <: OperatorTask]] = Array(new ProcessRunner(OutputManager.global))
  lazy val PATH_SEPARATOR: String = File.separator

  lazy val DEV_MODE: Boolean = getProperty(Key.DEV_MODE, "false").toBoolean
  lazy val OS: OS = {
    val sys = scala.util.Properties
    if (sys.isWin) WINDOWS
    else if (sys.isMac) MAC
    else LINUX
  }

  lazy val logger = "akka.event.slf4j.Slf4jLogger"
  implicit lazy val SCHEDULE_EXECUTION_CONTEXT: ExecutionContext = ExecutionContext.global

  def print(): Unit = {
    import scala.collection.JavaConversions.mapAsScalaMap
    println("config:")
    properties.foreach(kv => println(s"\t${kv._1} -> ${kv._2}"))
  }

  def getProperty(key: String): String = parse(properties.getProperty(key), properties)

  def getProperty(key: String, orElse: String): String = parse(properties.getProperty(key, orElse), properties)

  def load(files: Array[String]): Unit = {
    println("load:")
    for (file <- files) {
      val path = parsePath(file)
      println("\t" + path)
      properties.load(new FileInputStream(new File(path)))
    }
  }

  def load(key: String, value: String): Unit = {
    properties.put(key, value)
  }

  def loadArgs(args: Array[String]): Unit = {
    val arg: Map[String, String] = args
      .map(str => str.split("="))
      .map(arr => (arr(0), arr(1)))
      .toMap

    println("args: ")
    arg.foreach(kv => println("\t" + kv))

    load(Key.BASE_PATH, arg.getOrElse("--base-path", new File(".").getCanonicalPath))

    val paths = arg.getOrElse("--conf", "conf" / "application.properties,conf" / "application-env.properties")
      .split(",")
      .map(_.trim)
      .filter(_.nonEmpty)
    load(paths)

    val topics = arg
      .get("--topics")
      .map(str2arr)
      .getOrElse(Array.empty)
      .union(str2arr(getProperty(Key.CLUSTER_NODE_TOPICS)))
      .distinct
      .toSeq

    load(Key.CLUSTER_NODE_TOPICS, topics.mkString(","))

    val host = arg.getOrElse("--host", getProperty(Key.HOST))
    load(Key.HOST, host)

    val port = arg.getOrElse("--port", getProperty(Key.CLUSTER_NODE_PORT)).toInt
    load(Key.CLUSTER_NODE_PORT, port.toString)

    val seedNodes: Seq[String] = str2arr(arg
      .getOrElse("--seed-node", getProperty(Key.CLUSTER_NODE_SEEDS)))
      .map(_.split(":"))
      .map(arr => if (arr.length == 1) arr :+ "3551" else arr)
      .map(_.mkString(":"))
      .toSeq

    load(Key.CLUSTER_NODE_SEEDS, seedNodes.mkString(","))
    print()
  }

  private def str2arr(string: String): Array[String] = {
    string.split(",")
      .map(_.trim)
      .filter(_.nonEmpty)
  }

  private def parsePath(file: String): String = {
    if (file.startsWith("/"))
      file
    else
      BASE_PATH / file
  }

    val pattern: Regex = """(.*)\$\{(.*)}(.*)""".r  //新建一个正则表达式

  private def parse(line: String, prop: Properties): String = {
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

    val HOST: String = "server.host"
    val CLUSTER_NODE_SYSTEM_NAME: String = "cluster-node.system-name"
    val CLUSTER_NODE_PORT: String = "cluster-node.port"
    val CLUSTER_NODE_SEEDS: String = "cluster-node.seeds"
    val CLUSTER_NODE_TOPICS: String = "cluster-node.topics"
    val CLUSTER_NODE_METRICS_SAMPLE_INTERVAL = "cluster-node.metrics.sample-interval"
    val CLIENT_NODE_SYSTEM_NAME: String = "client-node.system-name"
    val CLIENT_NODE_PORT: String = "client-node.port"
    val REGISTRY_NODE_SYSTEM_NAME = "registry-node.system-name"
    val REGISTRY_NODE_PORT = "registry-node.port"
    val FILE_SERVER_PORT = "file-server.port"
    val FILE_SERVER_SYSTEM_NAME = "file-server.system-name"
    val FILE_SERVER_CHUNK_SIZE: String = "file-server.chunk-size"
    val FILE_SERVER_BASE_PATH: String = "file-server.base-path"
    val FILE_SERVER_CONTEXT_PATH = "file-server.context-path"
    val FILE_SERVER_RECURSIVE_TRANSFER_MAX = "file-server.recursive-transfer-max"
    val FILE_SERVER_DISPATCHER_CORE_POOL_SIZE_MIN = "file-server.dispatcher.core-pool-size-min"
    val FILE_SERVER_DISPATCHER_CORE_POOL_SIZE_FACTOR = "file-server.dispatcher.core-pool-size-factor"
    val FILE_SERVER_DISPATCHER_CORE_POOL_SIZE_MAX = "file-server.dispatcher.core-pool-size-max"
    val FILE_SERVER_HOST_CONNECTION_POOL_MAX_CONNECTIONS = "file-server.host-connection.pool.max-connections"
    val FILE_SERVER_HOST_CONNECTION_POOL_MIN_CONNECTIONS = "file-server.host-connection.pool.min-connections"
    val FILE_SERVER_HOST_CONNECTION_POOL_MAX_RETRIES = "file-server.host-connection.pool.max-retries"
    val FILE_SERVER_HOST_CONNECTION_POOL_MAX_OPEN_REQUESTS = "file-server.host-connection.pool.max-open-requests"
    val DEV_MODE: String = "dev-mode"

  }

  private def clusterSystem(): ActorSystem = {
    import scala.collection.JavaConversions.mapAsJavaMap
    val config = ConfigFactory
      .parseMap(Map(
        "akka.cluster.seed-nodes" -> seqAsJavaList(CLUSTER_NODE_SEEDS),
        "akka.remote.netty.tcp.hostname" -> HOST,
        "akka.remote.netty.tcp.port" -> CLUSTER_NODE_PORT,
        "akka.remote.netty.tcp.bind-hostname" -> HOST,
        "akka.remote.netty.tcp.bind-port" -> CLUSTER_NODE_PORT,
        "akka.cluster.metrics.collector.sample-interval" -> CLUSTER_NODE_METRICS_SAMPLE_INTERVAL,
        "akka.loggers" -> seqAsJavaList(Seq(CLUSTER_NODE_LOGGER))
      ))
      .withFallback(ConfigFactory.parseString(
        """
           |akka {
           | actor {
           |   provider = "akka.cluster.ClusterActorRefProvider"
           |   warn-about-java-serializer-usage = false
           |  }
           | remote {
           |  enabled-transports = ["akka.remote.netty.tcp"]
           |  log-remote-lifecycle-events = off
           | }
           | extensions = ["akka.cluster.client.ClusterClientReceptionist", "akka.cluster.metrics.ClusterMetricsExtension"]
           |}
           |task-runner-dispatcher {
           | type = PinnedDispatcher
           | executor = "thread-pool-executor"
           |}
           |""".stripMargin))
    ActorSystem.create(CLUSTER_NODE_SYSTEM_NAME, config)
  }

  private def clientSystem(): ActorSystem = {
    val configString =
      s"""
         |akka {
         | actor {
         |   provider = remote
         |   warn-about-java-serializer-usage = false
         |   serializers {
         |     java = "akka.serialization.JavaSerializer"
         |     proto = "akka.remote.serialization.ProtobufSerializer"
         |   }
         | }
         | loggers = ["$CLIENT_NODE_LOGGER"]
         | remote {
         |    enabled-transports = ["akka.remote.netty.tcp"]
         |    log-remote-lifecycle-events = off
         |    netty.tcp {
         |      hostname = "$HOST"
         |      port = $CLIENT_NODE_PORT
         |      bind-hostname = "$HOST"
         |      bind-port = $CLIENT_NODE_PORT
         |    }
         |  }
         |}
         |""".stripMargin
    ConfigFactory.parseString(configString)
    ActorSystem(CLIENT_NODE_SYSTEM_NAME, ConfigFactory.parseString(configString))
  }

  private def fileServerSystem(): ActorSystem = {
    val configString =
      s"""
         |file-io-dispatcher {
         |  type = Dispatcher
         |  executor = "thread-pool-executor"
         |  thread-pool-executor {
         |    core-pool-size-min = $FILE_SERVER_DISPATCHER_CORE_POOL_SIZE_MIN
         |    core-pool-size-factor = $FILE_SERVER_DISPATCHER_CORE_POOL_SIZE_FACTOR
         |    core-pool-size-max = $FILE_SERVER_DISPATCHER_CORE_POOL_SIZE_MAX
         |  }
         |  throughput = 1000
         |}
         |akka.loggers = ["$FILE_SERVER_LOGGER"]
         |
         |akka.http {
         |  host-connection-pool {
         |    max-connections = $FILE_SERVER_HOST_CONNECTION_POOL_MAX_CONNECTIONS
         |    min-connections = $FILE_SERVER_HOST_CONNECTION_POOL_MIN_CONNECTIONS
         |    min-retries = $FILE_SERVER_HOST_CONNECTION_POOL_MAX_RETRIES
         |    max-open-requests = $FILE_SERVER_HOST_CONNECTION_POOL_MAX_OPEN_REQUESTS
         |  }
         |}
         |""".stripMargin
    ConfigFactory.parseString(configString)
    ActorSystem(FILE_SERVER_SYSTEM_NAME, ConfigFactory.parseString(configString))
  }

  def main(args: Array[String]): Unit = {
    println(properties)
    import scala.collection.JavaConversions.asScalaSet
    properties.keySet().foreach(key => println(s"$key = ${properties.get(key)}"))
    val uri: File = new File(getProperty("HADOOP_CONF_DIR"))
    println(uri.getPath)
  }
}
