package com.oceanum.triger

import java.util.Date

import akka.actor.{ActorRef, ActorSystem, Props}
import com.oceanum.annotation.ITrigger
import com.oceanum.common.{ActorSystems, Environment}
import com.typesafe.akka.extension.quartz.{MessageRequireFireTime, QuartzSchedulerExtension}
import org.quartz.impl.DirectSchedulerFactory

/**
 * @author chenmingkun
 * @date 2020/7/26
 */
@ITrigger
class QuartzTrigger extends Trigger {

  private implicit val system: ActorSystem = ActorSystems.SYSTEM
  private val quartz: QuartzSchedulerExtension = QuartzSchedulerExtension(system)
  private val receiver: ActorRef = system.actorOf(Props[TriggerActor])

  override def start(name: String, config: Map[String, String])(action: Date => Unit): Unit = {
    startTrigger(name, config, receiver, TriggerAction(action))
  }

  private def startTrigger(name: String, config: Map[String, String], receiver: ActorRef, msg: TriggerAction): Unit = {
    val cron = config("cron")
    val startTime = config.get("startTime").map(str => new Date(str.toLong))
    val calendar = config.get("calendar")
    val description = config.get("description")
    quartz.createSchedule(name, description, cron, calendar, Environment.TIME_ZONE)
    quartz.schedule(name, receiver, MessageRequireFireTime(msg), startTime)
  }

  override def stop(name: String): Boolean = {
    quartz.unscheduleJob(name)
  }

  override def suspend(name: String): Boolean = {
    quartz.suspendJob(name)
  }

  override def resume(name: String): Boolean = {
    quartz.resumeJob(name)
  }

  override def triggerType: String = "QUARTZ"
}
