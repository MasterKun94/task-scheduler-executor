package com.oceanum.api

import akka.actor.{Actor, ActorLogging}
import akka.cluster.{Cluster, ClusterEvent}
import akka.cluster.ClusterEvent.{MemberEvent, UnreachableMember}
import com.oceanum.api.entities.Coordinator
import com.oceanum.common.SystemInit
import com.oceanum.persistence.Catalog

import scala.util.{Failure, Success}
import scala.collection.JavaConversions.mapAsJavaMap

class FallbackListener extends Actor with ActorLogging {
  private lazy val restService = SystemInit.restService
  private lazy val coordinatorRepo = Catalog.getRepository[Coordinator]
  private val cluster = Cluster(context.system)
  import com.oceanum.common.Environment.NONE_BLOCKING_EXECUTION_CONTEXT

  override def preStart(): Unit = {
    cluster.subscribe(self, ClusterEvent.initialStateAsEvents, classOf[UnreachableMember], classOf[MemberEvent])

  }

  override def receive: Receive = {
    case _:MemberEvent|UnreachableMember =>
      val future = restService
        .getClusterNodes(status = Some("up"), host = None, role = None)
        .map(_.nodes.map(_.host))
      future
        .flatMap { value =>
          coordinatorRepo.find(
            expr = "!repo.fieldIn('host', values)",
            env = Map("env" -> value)
          )
        }
        .andThen {
          case Failure(exception) =>
            log.error(exception, "fallback failed: " + exception.getMessage)
        }
  }
}
