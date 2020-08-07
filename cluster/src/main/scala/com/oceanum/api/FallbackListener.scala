package com.oceanum.api

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorLogging}
import akka.cluster.ClusterEvent.{MemberEvent, UnreachableMember}
import akka.cluster.{Cluster, ClusterEvent}
import com.oceanum.api.entities.{ClusterNodes, Coordinator}
import com.oceanum.common.{Scheduler, SystemInit}
import com.oceanum.persistence.Catalog

import scala.collection.JavaConversions.mapAsJavaMap
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success}

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
      val future: Future[ClusterNodes] = restService
        .getClusterNodes(status = Some("up"), host = None, role = None)
      future
        .flatMap { value: ClusterNodes =>
          coordinatorRepo
            .find(
              expr = "!repo.fieldIn('host', values)",
              env = Map("values" -> value.nodes.map(_.host))
            )
            .map(_.filter(coord => coord.endTime.map(_.getTime).getOrElse(Long.MaxValue) > System.currentTimeMillis()))
            .flatMap { coordinators =>
              val seq = coordinators
                .map(coord => {
                  val host = value.consistentHashSelect(coord.name).host
                  log.info("fallback: moving coordinator: [{}] to host: [{}]", coord, host)
                  coord.copy(host = host, version = coord.version + 1)
                }
                )
                .map(coord => {
                  val remoteRestService = RemoteRestServices.get(coord.host)
                  remoteRestService.submitCoordinator(coord)
                    .map { _ =>
                      Scheduler.scheduleOnce(FiniteDuration(5, TimeUnit.SECONDS)) {
                        remoteRestService.runCoordinator(coord.name)
                      }
                    }
                })
              Future.sequence(seq)
            }
        }
        .andThen {
          case Success(_) =>
            log.info("fallback complete")
          case Failure(exception) =>
            log.error(exception, "fallback failed: " + exception.getMessage)
        }
  }
}
