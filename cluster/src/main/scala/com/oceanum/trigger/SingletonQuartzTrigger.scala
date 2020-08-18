package com.oceanum.trigger

import java.util.Date
import java.util.concurrent.TimeUnit

import akka.pattern.ask
import akka.util.Timeout
import com.oceanum.annotation.ITrigger
import com.oceanum.common.{ActorSystems, CoordStatus}

import scala.concurrent.Future

//@ITrigger
class SingletonQuartzTrigger extends Trigger {
  private val singleton = SingletonQuartzActor.start(ActorSystems.SYSTEM)
  implicit private val timeout: Timeout = Timeout(20, TimeUnit.SECONDS)
  import com.oceanum.common.Environment.NONE_BLOCKING_EXECUTION_CONTEXT

  override def start(name: String, config: Map[String, String], startTime: Option[Date])(action: (Date, Map[String, Any]) => Unit): Future[Unit] = {
    singleton.ask(StartTrigger(name, config, startTime, action)).map(_ => Unit)
  }

  override def recover(name: String, config: Map[String, String], startTime: Option[Date], state: CoordStatus)(action: (Date, Map[String, Any]) => Unit): Future[Unit] = {
    Future(Unit)
  }

  override def stop(name: String): Future[Boolean] = {
    singleton.ask(StopTrigger(name)).mapTo[Boolean]
  }

  override def suspend(name: String): Future[Boolean] = {
    singleton.ask(SuspendTrigger(name)).mapTo[Boolean]
  }

  override def resume(name: String): Future[Boolean] = {
    singleton.ask(ResumeTrigger(name)).mapTo[Boolean]
  }

  override def triggerType: String = "SQUARTZ"
}

case class StartTrigger(name: String, config: Map[String, String], startTime: Option[Date], action: (Date, Map[String, Any]) => Unit)
case class SuspendTrigger(name: String)
case class ResumeTrigger(name: String)
case class StopTrigger(name: String)