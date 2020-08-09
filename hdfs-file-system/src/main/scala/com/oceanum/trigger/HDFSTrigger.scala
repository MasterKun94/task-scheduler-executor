package com.oceanum.trigger

import java.util.Date

import com.oceanum.annotation.ITrigger

@ITrigger
class HDFSTrigger extends Trigger {
  override def start(name: String, config: Map[String, String], startTime: Option[Date])(action: Date => Unit): Unit = ???

  override def stop(name: String): Boolean = ???

  override def suspend(name: String): Boolean = ???

  override def resume(name: String): Boolean = ???

  override def triggerType: String = "HDFS"
}
