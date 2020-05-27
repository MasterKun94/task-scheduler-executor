package com.oceanum.actors

import akka.actor.{Actor, ActorLogging}
import akka.cluster.ClusterEvent.{MemberEvent, MemberRemoved, MemberUp, UnreachableMember}
import akka.cluster.{Cluster, ClusterEvent}

/**
 * @author chenmingkun
 * @date 2020/5/3
 */
class ClusterNode extends Actor with ActorLogging {
  val cluster: Cluster = Cluster(context.system)

  override def preStart(): Unit = {
    cluster.subscribe(self, ClusterEvent.initialStateAsEvents, classOf[UnreachableMember], classOf[MemberEvent])
  }

  override def postStop(): Unit = cluster.unsubscribe(self)

  override def receive: Receive = {
    case MemberUp(member) => log.info("member up: " + member)

    case UnreachableMember(member) => log.info("unreachable member: " + member)

    case MemberRemoved(member, status) => log.info("member removed: " + member + ", status: " + status)

    case e: MemberEvent => log.info("member event: " + e)
  }
}