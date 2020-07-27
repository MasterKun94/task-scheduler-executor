package com.oceanum.trigger

import com.oceanum.common.Environment
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension

/**
 * @author chenmingkun
 * @date 2020/7/26
 */
class QuartzTrigger extends Trigger {

  private val quartz = QuartzSchedulerExtension(Environment.CLIENT_SYSTEM)

  override def trigger(): Unit = ???
}
