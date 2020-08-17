package com.oceanum.trigger

import java.util.Date

import akka.actor.{Actor, ActorRef, ActorSystem, PoisonPill, Props}
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings}
import com.oceanum.common.Environment
import com.oceanum.jdbc.expr.{ExprParser, JavaHashMap}
import com.typesafe.akka.`extension`.quartz.{MessageRequireFireTime, QuartzSchedulerExtension}

class SingletonQuartzActor extends Actor {

  override def preStart(): Unit = {
    val quartz: QuartzSchedulerExtension = QuartzSchedulerExtension(context.system)
    val receiver: ActorRef = context.system.actorOf(Props[TriggerActor])
    context.become(receive(quartz, receiver))
  }

  override def receive: Receive = {
    case _ =>
  }

  def receive(quartz: QuartzSchedulerExtension, receiver: ActorRef): Receive = {
    case StartTrigger(name, config, startTime, action) =>
      val cron = ExprParser.parse(config("cron"))(new JavaHashMap(0))
      val calendar = config.get("calendar")
        .map(str => ExprParser.parse(str)(new JavaHashMap(0)))
      val description = config.get("description")
        .map(str => ExprParser.parse(str)(new JavaHashMap(0)))
      if (quartz.runningJobs.contains(name)) {
        sender() ! new IllegalArgumentException(name + " is already running")
      } else {
        quartz.createSchedule(name, description, cron, calendar, Environment.TIME_ZONE)
        sender() ! quartz.schedule(name, receiver, MessageRequireFireTime(TriggerAction(action)), startTime)
      }

    case StopTrigger(name) =>
      sender() ! quartz.unscheduleJob(name)

    case SuspendTrigger(name) =>
      sender() ! quartz.suspendJob(name)

    case ResumeTrigger(name) =>
      sender() ! quartz.resumeJob(name)
  }
}

object SingletonQuartzActor {

  def start(system: ActorSystem): ActorRef = {
    system.actorOf(ClusterSingletonManager.props(
      singletonProps = Props(classOf[SingletonQuartzActor]),
      settings = ClusterSingletonManagerSettings(system),
      terminationMessage = PoisonPill.getInstance
    ), name = Environment.CLUSTER_NODE_METRICS_NAME)
  }
}
