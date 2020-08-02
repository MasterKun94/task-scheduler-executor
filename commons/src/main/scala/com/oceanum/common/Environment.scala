package com.oceanum.common

import java.io.{File, FileInputStream}
import java.util.concurrent.atomic.AtomicBoolean
import java.util.{Locale, Properties, TimeZone}

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.concurrent.duration.FiniteDuration
import com.oceanum.common.Implicits._

/**
 * @author chenmingkun
 * @date 2020/5/1
 */
object Environment {

  private val properties = new Properties()
  lazy val MODE: String = getProperty("mode", "client")
  lazy val LOCALE: Locale = Locale.ENGLISH
  lazy val TIME_ZONE: TimeZone = TimeZone.getTimeZone(getProperty("timezone", TimeZone.getDefault.getDisplayName))
  lazy val BASE_PATH: String = getBasePath(getProperty(Key.BASE_PATH, scala.util.Properties.userDir))
  lazy val EXEC_PYTHON: String = getProperty(Key.EXEC_PYTHON, "python")
  lazy val EXEC_PYTHON_ENABLED: Boolean = getProperty(Key.EXEC_PYTHON_ENABLED, "true").toBoolean
  lazy val EXEC_JAVA: String = getProperty(Key.EXEC_JAVA, scala.util.Properties.javaHome/"bin"/"java")
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
  lazy val EXEC_WORK_DIR: String = getProperty(Key.EXEC_WORK_DIR, BASE_PATH/ "com/oceanum/exec").toAbsolute()
  lazy val EXEC_THREAD_NUM: Int = getProperty(Key.EXEC_THREAD_NUM, "16").toInt
  lazy val EXEC_MAX_TIMEOUT: FiniteDuration = fd"${getProperty(Key.EXEC_MAX_TIMEOUT, "24h")}"
  lazy val EXEC_STATE_UPDATE_INTERVAL: String = "10s"
  lazy val EXEC_UN_REACH_TIMEOUT: Long = fd"100s".toMillis

  lazy val HOST: String = getProperty(Key.HOST, "127.0.0.1")
  lazy val CLUSTER_NODE_TASK_INIT_EXECUTOR: ExecutionContext = ExecutionContext.global
  lazy val CLUSTER_NODE_SYSTEM_NAME: String = getProperty(Key.CLUSTER_NODE_SYSTEM_NAME, "cluster")
  lazy val CLUSTER_NODE_PORT: Int = getProperty(Key.CLUSTER_NODE_PORT, "4551").toInt
  lazy val CLUSTER_NODE_SEEDS: Seq[String] = getProperty(Key.CLUSTER_NODE_SEEDS, s"$HOST:$CLUSTER_NODE_PORT").split(",").map(node => s"akka.tcp://$CLUSTER_NODE_SYSTEM_NAME@$node").toSeq
  lazy val CLUSTER_NODE_TOPICS: Seq[String] = getProperty(Key.CLUSTER_NODE_TOPICS, "default").split(",").map(_.trim).toSeq
  lazy val CLUSTER_NODE_METRICS_SAMPLE_INTERVAL: String = getProperty(Key.CLUSTER_NODE_METRICS_SAMPLE_INTERVAL, "5s")
  lazy val CLUSTER_NODE_METRICS_TOPIC: String = getProperty(Key.CLUSTER_NODE_METRICS_TOPIC, "cluster-node-metrics")
  lazy val CLUSTER_NODE_METRICS_NAME: String = getProperty(Key.CLUSTER_NODE_METRICS_NAME, "cluster-node-metrics")
  lazy val CLUSTER_NODE_METRICS_PING_INTERVAL: FiniteDuration = fd"${getProperty(Key.CLUSTER_NODE_METRICS_PING_INTERVAL, "20s")}"
  lazy val CLUSTER_NODE_METRICS_PING_TIMEOUT: FiniteDuration = fd"${getProperty(Key.CLUSTER_NODE_METRICS_PING_TIMEOUT, "100s")}"
  lazy val CLUSTER_NODE_LOGGER: String = getProperty(Key.CLUSTER_NODE_LOGGER, logger)

  lazy val CLIENT_NODE_SYSTEM_NAME: String = getProperty(Key.CLIENT_NODE_SYSTEM_NAME, "client")
  lazy val CLIENT_NODE_PORT: Int = getProperty(Key.CLIENT_NODE_PORT, "5551").toInt
  lazy val CLIENT_NODE_LOGGER: String = getProperty(Key.CLIENT_NODE_LOGGER, logger)

  lazy val GRAPH_FLOW_DEFAULT_PARALLELISM: Int = 1
  lazy val GRAPH_SOURCE_QUEUE_BUFFER_SIZE: Int = 100
  lazy val GRAPH_DEFAULT_EXECUTOR: ExecutionContext = ExecutionContext.global

  lazy val FILE_CLIENT_DEFAULT_SCHEME: String = getProperty(Key.FILE_CLIENT_DEFAULT_SCHEME, "hdfs")
  lazy val FILE_SERVER_CONTEXT_PATH: String = getProperty(Key.FILE_SERVER_CONTEXT_PATH, "file")
  lazy val FILE_SERVER_PORT: Int = getProperty(Key.FILE_SERVER_PORT, "7011").toInt
  lazy val FILE_SERVER_SYSTEM_NAME: String = getProperty(Key.FILE_SERVER_SYSTEM_NAME, "file-server")
  lazy val FILE_SERVER_CHUNK_SIZE: Int = getProperty(Key.FILE_SERVER_CHUNK_SIZE, "8192").toInt
  lazy val FILE_SERVER_BASE_PATH: String = fileServerBasePath(getProperty(Key.FILE_SERVER_BASE_PATH, if (OS == WINDOWS) "" else "/"))
  lazy val FILE_SERVER_RECURSIVE_TRANSFER_MAX: Int = getProperty(Key.FILE_SERVER_RECURSIVE_TRANSFER_MAX, "100").toInt
  lazy val FILE_SERVER_DISPATCHER_CORE_POOL_SIZE_MIN: Int = getProperty(Key.FILE_SERVER_DISPATCHER_CORE_POOL_SIZE_MIN, "6").toInt
  lazy val FILE_SERVER_DISPATCHER_CORE_POOL_SIZE_MAX: Int = getProperty(Key.FILE_SERVER_DISPATCHER_CORE_POOL_SIZE_MAX, "60").toInt
  lazy val FILE_SERVER_DISPATCHER_CORE_POOL_SIZE_FACTOR: Int = getProperty(Key.FILE_SERVER_DISPATCHER_CORE_POOL_SIZE_MIN, "5").toInt
  lazy val FILE_SERVER_HOST_CONNECTION_POOL_MAX_RETRIES: Int = getProperty(Key.FILE_SERVER_HOST_CONNECTION_POOL_MAX_RETRIES, "5").toInt
  lazy val FILE_SERVER_HOST_CONNECTION_POOL_MAX_CONNECTIONS: Int = getProperty(Key.FILE_SERVER_HOST_CONNECTION_POOL_MAX_CONNECTIONS, "12").toInt
  lazy val FILE_SERVER_HOST_CONNECTION_POOL_MIN_CONNECTIONS: Int = getProperty(Key.FILE_SERVER_HOST_CONNECTION_POOL_MIN_CONNECTIONS, "1").toInt
  lazy val FILE_SERVER_HOST_CONNECTION_POOL_MAX_OPEN_REQUESTS: Int = getProperty(Key.FILE_SERVER_HOST_CONNECTION_POOL_MAX_OPEN_REQUESTS, "64").toInt
  lazy val FILE_SERVER_LOGGER: String = getProperty(Key.FILE_SERVER_LOGGER, logger)
  lazy val TASK_INFO_TRIGGER_INTERVAL: FiniteDuration = fd"${getProperty(Key.CLUSTER_NODE_TASK_INFO_TRIGGER_INTERVAL, "20s")}"

  lazy val PATH_SEPARATOR: String = File.separator
  lazy val LOG_LOGBACK: String = getProperty(Key.LOG_LOGBACK, BASE_PATH/"conf"/"logback.xml").toAbsolute()
  lazy val LOG_FILE: String = logFile
  lazy val LOG_FILE_DIR: String = getProperty(Key.LOG_FILE_DIR, BASE_PATH/"log").toAbsolute()
  lazy val LOG_FILE_NAME: String = getProperty(Key.LOG_FILE_NAME, "task-executor-%d{yyyy-MM-dd}.log")
  lazy val LOG_FILE_PATTERN: String = getProperty(Key.LOG_FILE_PATTERN, "%date{ISO8601} %-5level %-46logger - %msg%n")
  lazy val LOG_FILE_MAX_HISTORY: String = getProperty(Key.LOG_FILE_MAX_HISTORY, "30")
  lazy val LOG_FILE_MAX_SIZE: String = getProperty(Key.LOG_FILE_MAX_SIZE, "10MB")
  lazy val LOG_STDOUT_PATTERN: String = getProperty(Key.LOG_STDOUT_PATTERN, "%date{ISO8601} %highlight(%-5level) %-46logger - %msg%n")

  lazy val HADOOP_HOME: String = getProperty(Key.HADOOP_HOME, findHadoopHome).toPath()
  lazy val HADOOP_FS_URL: String = getProperty(Key.HADOOP_FS_URL, "hdfs://localhost:9000")
  lazy val HADOOP_USER: String = getProperty(Key.HADOOP_USER, "root")
  lazy val HADOOP_BUFFER_SIZE: Int = getProperty(Key.HADOOP_BUFFER_SIZE, "8192").toInt
  lazy val DEV_MODE: Boolean = getProperty(Key.DEV_MODE, "false").toBoolean
  lazy val logger = "akka.event.slf4j.Slf4jLogger"
  lazy val SCHEDULE_EXECUTION_CONTEXT: ExecutionContext = ExecutionContext.global
  lazy val AVIATOR_CACHE_ENABLED: Boolean = true
  lazy val AVIATOR_CACHE_CAPACITY: Int = 10000
  lazy val OS: OS = {
    val sys = scala.util.Properties
    if (sys.isWin) WINDOWS
    else if (sys.isMac) MAC
    else LINUX
  }

  implicit lazy val NONE_BLOCKING_EXECUTION_CONTEXT: ExecutionContextExecutor = ActorSystems.SYSTEM.dispatcher

  def printEnv(): Unit = {
    import scala.collection.JavaConversions.asScalaSet
    println("config:")
    properties.keySet().foreach(k => println(s"\t$k -> ${getProperty(k.toString)}"))
  }

  def getProperty(key: String): String = {
    PropertyParser.parse(properties.getProperty(key))(properties)
  }

  def getProperty(key: String, orElse: => String): String = {
    PropertyParser.parse(properties.getProperty(key, orElse))(properties)
  }

  def setProperty(key: String, value: String): Unit = {
    properties.setProperty(key, value)
    System.setProperty("conf." + key, value)
  }

  private val loaded = new AtomicBoolean(false)
  def loadEnv(args: Array[String]): Unit = {
    if (loaded.get()) {
      return
    }
    val arg: Map[String, String] = args
      .map(str => str.split("="))
      .map(arr => (arr(0), arr(1)))
      .toMap

    setProperty(Key.BASE_PATH, arg.getOrElse(Arg.BASE_PATH, scala.util.Properties.userDir))

    val path = BASE_PATH/"conf"/"application.properties"
    val paths = arg.getOrElse(Arg.CONF, path)
      .split(",")
      .map(_.trim)
      .filter(_.nonEmpty)
      .filter(p => new File(p).exists())
    load(paths)

    val topics = arg
      .get(Arg.TOPICS)
      .map(str2arr)
      .getOrElse(Array.empty)
      .union(str2arr(getProperty(Key.CLUSTER_NODE_TOPICS)))
      .distinct
      .toSeq

    if (topics.nonEmpty) {
      setProperty(Key.CLUSTER_NODE_TOPICS, topics.mkString(","))
    }

    val host = arg.getOrElse(Arg.HOST, HOST)
    setProperty(Key.HOST, host)

    val port = arg.getOrElse(Arg.PORT, CLUSTER_NODE_PORT.toString)
    setProperty(Key.CLUSTER_NODE_PORT, port)

    val clientPort = arg.getOrElse(Arg.CLIENT_PORT, CLIENT_NODE_PORT.toString)
    setProperty(Key.CLIENT_NODE_PORT, clientPort)

    val mode = arg.getOrElse(Arg.MODE, MODE)
    setProperty(Key.MODE, mode)

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

    setProperty(Key.CLUSTER_NODE_SEEDS, seedNodes.mkString(","))
    setProperty(Key.LOG_FILE, LOG_FILE)
    setProperty(Key.LOG_FILE_MAX_HISTORY, LOG_FILE_MAX_HISTORY)
    setProperty(Key.LOG_FILE_MAX_SIZE, LOG_FILE_MAX_SIZE)
    setProperty(Key.LOG_FILE_PATTERN, LOG_FILE_PATTERN)
    setProperty(Key.LOG_STDOUT_PATTERN, LOG_STDOUT_PATTERN)

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

  def initSystem(): Unit = SystemInit.initAnnotatedClass()

  object Arg {
    val MODE = "--mode"
    val BASE_PATH = "--base-path"
    val TOPICS = "--topics"
    val HOST = "--host"
    val PORT = "--port"
    val SEED_NODE = "--seed-node"
    val CONF = "--conf"
    val CLIENT_PORT = "--client-port"
  }

  object Key {
    val MODE: String = "mode"
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

  private def getBasePath(path: String): String = new File(path).getAbsolutePath
  private def fileServerBasePath(path: String): String = if (path.trim.isEmpty && OS != WINDOWS) "/" else path

  private def load(files: Array[String]): Unit = {
    for (file <- files) {
      val path = parsePath(file)
      println("load: " + path)
      properties.load(new FileInputStream(new File(path)))
    }
  }

  private def findHadoopHome: String = {
    val p1 = System.getProperty("HADOOP_HOME", System.getenv("HADOOP_HOME"))
    if (p1 == null || p1.trim.isEmpty) {
      Array("/opt/cloudera/parcels/CDH/lib/hadoop").find(s => new File(s).isDirectory) match {
        case Some(s) => s
        case None => throw new IllegalArgumentException("没有找到HADOOP_HOME")
      }
    } else {
      p1
    }
  }

  private def logFile: String = LOG_FILE_DIR/LOG_FILE_NAME

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
}
