package com.oceanum.trigger

import java.util.Date

import akka.actor.{ActorRef, ActorSystem, Props}
import com.oceanum.annotation.ITrigger
import com.oceanum.common.{ActorSystems, CoordStatus, Environment}
import com.oceanum.expr.{ExprParser, JavaHashMap}
import com.typesafe.akka.extension.quartz.{MessageRequireFireTime, QuartzSchedulerExtension}

import scala.collection.concurrent.TrieMap

/**
 * Quartz触发器
 *
 * @author chenmingkun
 */
@ITrigger
class QuartzTrigger extends Trigger {

  private lazy implicit val system: ActorSystem = ActorSystems.SYSTEM
  private lazy val quartz: QuartzSchedulerExtension = QuartzSchedulerExtension(system)
  private lazy val receiver: ActorRef = system.actorOf(Props[TriggerActor])
  private val externalSuspendedTask: TrieMap[String, () => Unit] = TrieMap()

  /**
   * @param config 任务配置, 有三个参数分别是：
   *               cron：必须项，表示任务的执行计划；
   *               startTime：可选，表示任务第一次启动的时间；
   *               calendar：可选，暂未支持
   */
  override def start(name: String, config: Map[String, String], startTime: Option[Date])(action: Date => Unit): Unit = {
    startTrigger(name, config, startTime, receiver, TriggerAction(action))
  }

  private def startTrigger(name: String, config: Map[String, String], startTime: Option[Date], receiver: ActorRef, msg: TriggerAction): Unit = {
    val cron = ExprParser.parse(config("cron"))(new JavaHashMap(0))
    val calendar = config.get("calendar")
      .map(str => ExprParser.parse(str)(new JavaHashMap(0)))
    val description = config.get("description")
      .map(str => ExprParser.parse(str)(new JavaHashMap(0)))
    if (quartz.runningJobs.contains(name)) {
      throw new IllegalArgumentException(name + "is already running")
    }
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
    externalSuspendedTask.remove(name) match {
      case Some(func) =>
        func()
        true
      case None =>
        quartz.resumeJob(name)
    }
  }

  override def triggerType: String = "QUARTZ"

  override def recover(name: String, config: Map[String, String], startTime: Option[Date], status: CoordStatus)(action: Date => Unit): Unit = {
    if (status == CoordStatus.SUSPENDED) {
      val time: Option[Date] = startTime.orElse(Some(new Date()))
      externalSuspendedTask += (name -> (() => start(name, config, time)(action)))
    } else if (status == CoordStatus.RUNNING) {
      start(name, config, startTime)(action)
    }
  }
}
