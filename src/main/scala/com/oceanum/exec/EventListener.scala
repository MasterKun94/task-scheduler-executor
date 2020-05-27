package com.oceanum.exec

import org.slf4j.{Logger, LoggerFactory}

/**
 * @author chenmingkun
 * @date 2020/4/30
 */
trait EventListener {
  def prepare(message: Any = "")

  def start(message: Any = "")

  def running(message: Any = "")

  def failed(message: Any = "")

  def success(message: Any = "")

  def retry(message: Any = "")

  def timeout(message: Any = "")

  def kill(message: Any = "")
}

object EventListener extends Enumeration {
  type State = Value
  val OFFLINE, PREPARE, START, RUNNING, FAILED, SUCCESS, RETRY, TIMEOUT, KILL = Value

  def logHandler(): EventListener = new EventListener {
    val logger: Logger = LoggerFactory.getLogger(this.getClass)
    override def prepare(message: Any = ""): Unit = logger.info("task prepare, message: [{}]", message)

    override def running(message: Any = ""): Unit = logger.info("task running, message: [{}]", message)

    override def failed(message: Any = ""): Unit = logger.info("task failed, message: [{}]", message)

    override def success(message: Any = ""): Unit = logger.info("task success, message: [{}]", message)

    override def retry(message: Any = ""): Unit = logger.info("task retry, message: [{}]", message)

    override def kill(message: Any = ""): Unit = logger.info("task killed, message: [{}]", message)

    override def timeout(message: Any = ""): Unit = logger.info("task timeout, message: [{}]", message)

    override def start(message: Any = ""): Unit = logger.info("task start, message: [{}]", message)
  }
}
