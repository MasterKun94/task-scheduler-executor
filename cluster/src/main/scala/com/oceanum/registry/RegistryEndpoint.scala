package com.oceanum.registry

import akka.actor.{Actor, ActorLogging}
import akka.cluster.ClusterEvent.{MemberEvent, UnreachableMember}
import akka.cluster.{Cluster, ClusterEvent, Member, MemberStatus}
import com.oceanum.common.ConsistentHashing

/**
 * @author chenmingkun
 * @date 2020/8/2
 */
class RegistryEndpoint extends Actor with ActorLogging {
  private val cluster: Cluster = Cluster(context.system)

  override def preStart(): Unit = {
    cluster.subscribe(self, ClusterEvent.initialStateAsEvents, classOf[MemberEvent], classOf[UnreachableMember])
    context.become(receive(getConsistentHashing))
  }

  override def receive: Receive = {
    case _ =>
  }

  def receive(hashing: ConsistentHashing[Member]): Receive = {
    case _: MemberEvent | UnreachableMember =>
      context.become(receive(getConsistentHashing))
    case _ =>
      ??? // TODO
  }

  def getConsistentHashing: ConsistentHashing[Member] = new ConsistentHashing[Member](
    cluster.state.members.filter(_.status == MemberStatus.up),
    _.uniqueAddress.address.hostPort)
}
