package com.oceanum.client.actors

import akka.actor._
import akka.cluster.client._

/**
 * @author chenmingkun
 * @date 2020/5/3
 */
class ClientListener(clusterClient: ActorRef) extends Actor with ActorLogging {
  override def preStart(): Unit = {
    clusterClient ! SubscribeContactPoints
    super.preStart()
  }

  override def receive: Receive = {
    case ContactPoints(cps) =>
      cps.map {ap => log.info(s"*******ContactPoints:${ap.address.toString}******")}
    case ContactPointAdded(cp) =>
      log.info(s"*******ContactPointAdded: ${cp.address.toString}*******")
    case ContactPointRemoved(cp) =>
      log.info(s"*******ContactPointRemoved: ${cp.address.toString}*******")

  }
}


