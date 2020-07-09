package com.oceanum.common

import java.io.{File, FileInputStream}
import java.util.Properties
import java.util.concurrent.atomic.AtomicBoolean

import akka.actor.ActorSystem
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import com.oceanum.cluster.exec.{OperatorTask, TypedRunner}
import com.oceanum.cluster.runners.ProcessRunner
import com.oceanum.common.Implicits.{DurationHelper, PathHelper}
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

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

  lazy val AKKA_CONF: String = parsePath(getProperty(Key.AKKA_CONF, BASE_PATH/"conf"/"application.conf"))
  lazy val BASE_PATH: String = getBasePath(getProperty(Key.BASE_PATH, SystemProp.userDir))
  lazy val EXEC_PYTHON: String = getProperty(Key.EXEC_PYTHON, "python")
  lazy val EXEC_PYTHON_ENABLED: Boolean = getProperty(Key.EXEC_PYTHON_ENABLED, "true").toBoolean
  lazy val EXEC_JAVA: String = getProperty(Key.EXEC_JAVA, SystemProp.javaHome/"bin"/"java")
  lazy val EXEC_JAVA_ENABLED: Boolean = getProperty(Key.EXEC_JAVA_ENABLED, "false").toBoolean
  lazy val EXEC_SCALA: String = getProperty(Key.EXEC_SCALA, "scala")
  lazy val EXEC_SCALA_ENABLED: Boolean = getProperty(Key.EXEC_SCALA_ENABLED, "false").toBoolean
  lazy val EXEC_SHELL: String = getProperty(Key.EXEC_SHELL, "sh")
  lazy val EXEC_SHELL_ENABLED: Boolean = getProperty(Key.EXEC_SHELL_ENABLED, "true").toBoolean
  lazy val EXEC_SPARK: String = getProperty(Key.EXEC_SPARK, "spark-submit")
  lazy val EXEC_SPARK_ENABLED: String = getProperty(Key.EXEC_SPARK_ENABLED, "false")
  lazy val EXEC_SPARK_HOME: String = getProperty(Key.EXEC_SPARK_HOME)
  lazy val EXEC_DEFAULT_USER: String = getProperty(Key.EXEC_DEFAULT_USER, "root")
  lazy val EXEC_DEFAULT_RETRY_INTERVAL: String = getProperty(Key.EXEC_DEFAULT_RETRY_INTERVAL)
  lazy val EXEC_DEFAULT_RETRY_MAX: Int = getProperty(Key.EXEC_DEFAULT_RETRY_MAX, "1").toInt
  lazy val EXEC_DEFAULT_PRIORITY: Int = getProperty(Key.EXEC_DEFAULT_PRIORITY, "5").toInt
  lazy val EXEC_DEFAULT_TOPIC: String = getProperty(Key.EXEC_DEFAULT_TOPIC, "default")
  lazy val EXEC_WORK_DIR: String = getProperty(Key.EXEC_WORK_DIR, BASE_PATH/"exec").toAbsolute()
  lazy val EXEC_THREAD_NUM: Int = getProperty(Key.EXEC_THREAD_NUM, "16").toInt
  lazy val EXEC_MAX_TIMEOUT: FiniteDuration = fd"${getProperty(Key.EXEC_MAX_TIMEOUT, "24h")}"

  lazy val HOST: String = getProperty(Key.HOST, "127.0.0.1")
  lazy val GLOBAL_EXECUTOR: ExecutionContext = ExecutionContext.global
  lazy val CLUSTER_NODE_SYSTEM_NAME: String = getProperty(Key.CLUSTER_NODE_SYSTEM_NAME, "cluster")
  lazy val CLUSTER_NODE_PORT: Int = getProperty(Key.CLUSTER_NODE_PORT, "3551").toInt
  lazy val CLUSTER_NODE_SEEDS: Seq[String] = getProperty(Key.CLUSTER_NODE_SEEDS, s"127.0.0.1:$CLUSTER_NODE_PORT").split(",").map(node => s"akka.tcp://$CLUSTER_NODE_SYSTEM_NAME@$node").toSeq
  lazy val CLUSTER_NODE_TOPICS: Seq[String] = getProperty(Key.CLUSTER_NODE_TOPICS, "default").split(",").map(_.trim).toSeq
  lazy val CLUSTER_NODE_SYSTEM: ActorSystem = clusterSystem()
  lazy val CLUSTER_NODE_METRICS_SAMPLE_INTERVAL: String = getProperty(Key.CLUSTER_NODE_METRICS_SAMPLE_INTERVAL, "5s")
  lazy val CLUSTER_NODE_METRICS_TOPIC: String = getProperty(Key.CLUSTER_NODE_METRICS_TOPIC, "cluster-node-metrics")
  lazy val CLUSTER_NODE_METRICS_NAME: String = getProperty(Key.CLUSTER_NODE_METRICS_NAME, "cluster-node-metrics")
  lazy val CLUSTER_NODE_METRICS_PING_INTERVAL: FiniteDuration = fd"${getProperty(Key.CLUSTER_NODE_METRICS_PING_INTERVAL, "20s")}"
  lazy val CLUSTER_NODE_METRICS_PING_TIMEOUT: FiniteDuration = fd"${getProperty(Key.CLUSTER_NODE_METRICS_PING_TIMEOUT, "100s")}"
  lazy val CLUSTER_NODE_LOGGER: String = getProperty(Key.CLUSTER_NODE_LOGGER, logger)
  lazy val CLUSTER_NODE_RUNNER_STDOUT_HANDLER_CLASS: Class[_] = Class.forName(getProperty(Key.CLUSTER_NODE_RUNNER_STDOUT_HANDLER_CLASS, "com.oceanum.cluster.exec.StdoutFileOutputHandler"))
  lazy val CLUSTER_NODE_RUNNER_STDERR_HANDLER_CLASS: Class[_] = Class.forName(getProperty(Key.CLUSTER_NODE_RUNNER_STDERR_HANDLER_CLASS, "com.oceanum.cluster.exec.StderrFileOutputHandler"))
  lazy val CLUSTER_NODE_RUNNERS_CLASSES: Set[Class[_]] = str2arr(getProperty(Key.CLUSTER_NODE_RUNNER_CLASSES, "com.oceanum.cluster.runners.ProcessRunner")).map(Class.forName).toSet

  lazy val CLIENT_NODE_SYSTEM_NAME: String = getProperty(Key.CLIENT_NODE_SYSTEM_NAME, "client")
  lazy val CLIENT_NODE_PORT: Int = getProperty(Key.CLIENT_NODE_PORT, "4551").toInt
  lazy val CLIENT_NODE_LOGGER: String = getProperty(Key.CLIENT_NODE_LOGGER, logger)
  lazy val CLIENT_SYSTEM: ActorSystem = clientSystem()

  lazy val FILE_CLIENT_DEFAULT_SCHEME: String = getProperty(Key.FILE_CLIENT_DEFAULT_SCHEME, "cluster")
  lazy val FILE_CLIENT_CLASSES: Set[Class[_]] = str2arr(getProperty(Key.FILE_CLIENT_CLASSES, "com.oceanum.file.ClusterFileClient")).toSet.map(Class.forName)
  lazy val FILE_SERVER_SYSTEM: ActorSystem = fileServerSystem()
  lazy val FILE_SERVER_CONTEXT_PATH: String = getProperty(Key.FILE_SERVER_CONTEXT_PATH, "file")
  lazy val FILE_SERVER_PORT: Int = getProperty(Key.FILE_SERVER_PORT, "7011").toInt
  lazy val FILE_SERVER_SYSTEM_NAME: String = getProperty(Key.FILE_SERVER_SYSTEM_NAME, "file-server")
  lazy val FILE_SERVER_CHUNK_SIZE: Int = getProperty(Key.FILE_SERVER_CHUNK_SIZE, "8192").toInt
  lazy val FILE_SERVER_BASE_PATH: String = fileServerBasePath(getProperty(Key.FILE_SERVER_BASE_PATH, if (OS == WINDOWS) "" else "/"))
  lazy val FILE_SERVER_RECURSIVE_TRANSFER_MAX: Int = getProperty(Key.FILE_SERVER_RECURSIVE_TRANSFER_MAX, "100").toInt
  lazy val FILE_SERVER_DISPATCHER_CORE_POOL_SIZE_MIN: Int = getProperty(Key.FILE_SERVER_DISPATCHER_CORE_POOL_SIZE_MIN, "6").toInt
  lazy val FILE_SERVER_DISPATCHER_CORE_POOL_SIZE_FACTOR: Int = getProperty(Key.FILE_SERVER_DISPATCHER_CORE_POOL_SIZE_MIN, "5").toInt
  lazy val FILE_SERVER_DISPATCHER_CORE_POOL_SIZE_MAX: Int = getProperty(Key.FILE_SERVER_DISPATCHER_CORE_POOL_SIZE_MAX, "60").toInt
  lazy val FILE_SERVER_HOST_CONNECTION_POOL_MAX_CONNECTIONS: Int = getProperty(Key.FILE_SERVER_HOST_CONNECTION_POOL_MAX_CONNECTIONS, "12").toInt
  lazy val FILE_SERVER_HOST_CONNECTION_POOL_MIN_CONNECTIONS: Int = getProperty(Key.FILE_SERVER_HOST_CONNECTION_POOL_MIN_CONNECTIONS, "1").toInt
  lazy val FILE_SERVER_HOST_CONNECTION_POOL_MAX_RETRIES: Int = getProperty(Key.FILE_SERVER_HOST_CONNECTION_POOL_MAX_RETRIES, "5").toInt
  lazy val FILE_SERVER_HOST_CONNECTION_POOL_MAX_OPEN_REQUESTS: Int = getProperty(Key.FILE_SERVER_HOST_CONNECTION_POOL_MAX_OPEN_REQUESTS, "64").toInt
  lazy val FILE_SERVER_LOGGER: String = getProperty(Key.FILE_SERVER_LOGGER, logger)
  lazy val TASK_INFO_TRIGGER_INTERVAL: FiniteDuration = fd"${getProperty(Key.CLUSTER_NODE_TASK_INFO_TRIGGER_INTERVAL, "20s")}"

  lazy val TASK_RUNNERS: Array[TypedRunner[_ <: OperatorTask]] = Array(new ProcessRunner())
  lazy val PATH_SEPARATOR: String = File.separator
  lazy val LOG_LOGBACK: String = getProperty(Key.LOG_LOGBACK, BASE_PATH/"conf"/"logback.xml").toAbsolute()
  lazy val LOG_FILE: String = LOG_FILE_DIR/LOG_FILE_NAME
  lazy val LOG_FILE_DIR: String = getProperty(Key.LOG_FILE_DIR, BASE_PATH/"log").toAbsolute()
  lazy val LOG_FILE_NAME: String = getProperty(Key.LOG_FILE_NAME, "task-executor.%d{yyyy-MM-dd}.log")
  lazy val LOG_FILE_PATTERN: String = getProperty(Key.LOG_FILE_PATTERN, "%date{ISO8601} %-5level %-46logger - %msg%n")
  lazy val LOG_FILE_MAX_HISTORY: String = getProperty(Key.LOG_FILE_MAX_HISTORY, "30")
  lazy val LOG_FILE_MAX_SIZE: String = getProperty(Key.LOG_FILE_MAX_SIZE, "10MB")
  lazy val LOG_STDOUT_PATTERN: String = getProperty(Key.LOG_STDOUT_PATTERN, "%date{ISO8601} %highlight(%-5level) %-46logger - %msg%n")

  lazy val HADOOP_HOME: String = getProperty(Key.HADOOP_HOME, System.getenv("HADOOP_HOME")).toPath()
  lazy val HADOOP_FS_URL: String = getProperty(Key.HADOOP_FS_URL, "hdfs://localhost:9000")
  lazy val HADOOP_USER: String = getProperty(Key.HADOOP_USER, "root")
  lazy val HADOOP_BUFFER_SIZE: Int = getProperty(Key.HADOOP_BUFFER_SIZE, "8192").toInt
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
    import scala.collection.JavaConversions.asScalaSet
    println("config:")
    properties.keySet().foreach(k => println(s"\t$k -> ${getProperty(k.toString)}"))
    FILE_CLIENT_CLASSES
  }

  def getProperty(key: String): String = {
    parse(properties.getProperty(key), properties)
  }

  def getProperty(key: String, orElse: => String): String = {
    parse(properties.getProperty(key, orElse), properties)
  }

  private def getBasePath(path: String): String = new File(path).getAbsolutePath
  private def fileServerBasePath(path: String): String = if (path.trim.isEmpty && OS != WINDOWS) "/" else path

  private def load(files: Array[String]): Unit = {
    for (file <- files) {
      val path = parsePath(file)
      println("load: " + path)
      properties.load(new FileInputStream(new File(path)))
    }
  }

  def load(key: String, value: String): Unit = {
    properties.put(key, value)
    System.setProperty("conf." + key, value)
  }

  private val loaded: AtomicBoolean = new AtomicBoolean(false)
  def loadArgs(args: Array[String]): Unit = {
    if (loaded.get()) {
      return
    }
    val arg: Map[String, String] = args
      .map(str => str.split("="))
      .map(arr => (arr(0), arr(1)))
      .toMap

    load(Key.BASE_PATH, arg.getOrElse(Arg.BASE_PATH, new File(".").getCanonicalPath))

    val path = BASE_PATH/"conf"/"application.properties"
    val paths = arg.getOrElse(Arg.CONF, path)
      .split(",")
      .map(_.trim)
      .filter(_.nonEmpty)
    load(paths)

    val topics = arg
      .get(Arg.TOPICS)
      .map(str2arr)
      .getOrElse(Array.empty)
      .union(str2arr(getProperty(Key.CLUSTER_NODE_TOPICS)))
      .distinct
      .toSeq

    if (topics.nonEmpty) {
      load(Key.CLUSTER_NODE_TOPICS, topics.mkString(","))
    }

    val host = arg.getOrElse(Arg.HOST, HOST)
    load(Key.HOST, host)

    val port = arg.getOrElse(Arg.PORT, CLUSTER_NODE_PORT.toString).toInt
    load(Key.CLUSTER_NODE_PORT, port.toString)

    val seedNodes: Seq[String] = arg
      .get(Arg.SEED_NODE)
      .map(str2arr)
      .map(_
        .map(_.split(":"))
        .map(arr => if (arr.length == 1) arr :+ CLUSTER_NODE_PORT else arr)
        .map(_.mkString(":"))
        .toSeq
      )
      .getOrElse(CLUSTER_NODE_SEEDS)

    load(Key.CLUSTER_NODE_SEEDS, seedNodes.mkString(","))
    load(Key.LOG_FILE, LOG_FILE)
    load(Key.LOG_FILE_MAX_HISTORY, LOG_FILE_MAX_HISTORY)
    load(Key.LOG_FILE_MAX_SIZE, LOG_FILE_MAX_SIZE)
    load(Key.LOG_FILE_PATTERN, LOG_FILE_PATTERN)
    load(Key.LOG_STDOUT_PATTERN, LOG_STDOUT_PATTERN)

    val logback = new File(LOG_LOGBACK)
    if (logback.exists()) {
      val configurator = new JoranConfigurator()
      val lc = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
      configurator.setContext(lc)
      lc.reset()
      configurator.doConfigure(logback)
    }
    loaded.set(true)
  }

  private def str2arr(string: String): Array[String] = {
    string.split(",")
      .map(_.trim)
      .filter(_.nonEmpty)
  }

  private def parsePath(file: String): String = {
      if (file.toFile.isAbsolute)
        file.toAbsolute()
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

  object Arg {
    val BASE_PATH = "--base-path"
    val TOPICS = "--topics"
    val HOST = "--host"
    val PORT = "--port"
    val SEED_NODE = "--seed-node"
    val CONF = "--conf"
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
    val EXEC_DEFAULT_USER: String = "exec.default.user"
    val EXEC_DEFAULT_RETRY_INTERVAL: String = "exec.default.retry.interval"
    val EXEC_DEFAULT_RETRY_MAX: String = "exec.default.retry.max"
    val EXEC_DEFAULT_PRIORITY: String = "exec.default.priority"
    val EXEC_DEFAULT_TOPIC: String = "exec.default.topic"
    val EXEC_WORK_DIR: String = "exec.work-dir"
    val EXEC_THREAD_NUM: String = "exec.thread-num"
    val EXEC_MAX_TIMEOUT: String = "exec.max-timeout"

    val HOST: String = "server.host"
    val CLUSTER_NODE_SYSTEM_NAME: String = "cluster-node.system-name"
    val CLUSTER_NODE_PORT: String = "cluster-node.port"
    val CLUSTER_NODE_SEEDS: String = "cluster-node.seeds"
    val CLUSTER_NODE_TOPICS: String = "cluster-node.topics"
    val CLUSTER_NODE_METRICS_SAMPLE_INTERVAL = "cluster-node.metrics.sample-interval"
    val CLUSTER_NODE_METRICS_TOPIC: String = "cluster-node.metrics.topic"
    val CLUSTER_NODE_METRICS_NAME: String = "cluster-node.metrics.name"
    val CLUSTER_NODE_METRICS_PING_INTERVAL: String = "cluster-node.metrics.ping.interval"
    val CLUSTER_NODE_METRICS_PING_TIMEOUT: String = "cluster-node.metrics.ping.timeout"
    val CLUSTER_NODE_RUNNER_STDOUT_HANDLER_CLASS: String = "cluster-node.runner.stdout-handler.class"
    val CLUSTER_NODE_RUNNER_STDERR_HANDLER_CLASS: String = "cluster-node.runner.stderr-handler.class"
    val CLUSTER_NODE_TASK_INFO_TRIGGER_INTERVAL: String = "cluster-node.task-info.trigger.interval"
    val CLUSTER_NODE_LOGGER: String = "cluster-node.logger"
    val CLUSTER_NODE_RUNNER_CLASSES: String = "cluster-node.runner.classes"
    val CLIENT_NODE_SYSTEM_NAME: String = "client-node.system-name"
    val CLIENT_NODE_PORT: String = "client-node.port"
    val CLIENT_NODE_LOGGER: String = "client-node.logger"
    val FILE_CLIENT_DEFAULT_SCHEME: String = "file-client.default-scheme"
    val FILE_CLIENT_CLASSES: String = "file-client.classes"
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
    val FILE_SERVER_LOGGER = "file-server.logger"
    val DEV_MODE: String = "dev-mode"
    val LOG_LOGBACK = "log.logback"
    val LOG_FILE_DIR = "log.file.dir"
    val LOG_FILE_NAME = "log.file.name"
    val LOG_FILE = "log.file"
    val LOG_FILE_PATTERN = "log.file.pattern"
    val LOG_FILE_MAX_HISTORY = "log.file.max-history"
    val LOG_FILE_MAX_SIZE = "log.file.max-size"
    val LOG_STDOUT_PATTERN = "log.stdout.pattern"
    val HADOOP_HOME = "hadoop.home"
    val HADOOP_FS_URL = "hadoop.fs.url"
    val HADOOP_USER = "hadoop.user"
    val HADOOP_BUFFER_SIZE = "hadoop.buffer.size"
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
}
