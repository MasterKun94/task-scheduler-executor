package com.oceanum.triger

import akka.actor.ActorSystem
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension

/**
 * @author chenmingkun
 * @date 2020/7/26
 */
class QuartzTrigger(system: ActorSystem) extends Trigger {

  private val quartz = QuartzSchedulerExtension(system)

  override def trigger(): Unit = ???
}
