package com.oceanum.cluster

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.cluster.client.{ClusterClientUnreachable, ClusterClientUp, ClusterClients, SubscribeClusterClients}

class ReceptionistListener(receptionist: ActorRef) extends Actor with ActorLogging {
  override def preStart(): Unit = {
    receptionist ! SubscribeClusterClients
    super.preStart()
  }

  override def receive: Receive = {
    case ClusterClients(cs) =>
      cs.map{aref => println(s"*******ClusterClients: ${aref.path.address.toString}*******")}
    case ClusterClientUp(cc) =>
      log.info(s"*******ClusterClientUp: ${cc.path.address.toString}*******")
    case ClusterClientUnreachable(cc) =>
      log.info(s"*******ClusterClientUnreachable: ${cc.path.address.toString}*******")

  }
}