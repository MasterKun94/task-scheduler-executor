package com.oceanum.api

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, PoisonPill, Props}
import akka.cluster.ClusterEvent.{MemberDowned, MemberEvent, MemberExited, MemberLeft, MemberRemoved, UnreachableMember}
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings}
import akka.cluster.{Cluster, ClusterEvent}
import com.oceanum.api.entities.{ClusterNodes, Coordinator}
import com.oceanum.common.{NodeStatus, SystemInit}
import com.oceanum.persistence.Catalog

import scala.collection.JavaConversions.mapAsJavaMap
import scala.concurrent.Future
import scala.util.Failure

class FallbackListener extends Actor with ActorLogging {
  private lazy val restService = SystemInit.restService
  private lazy val coordinatorRepo = Catalog.getRepository[Coordinator]
  private val cluster = Cluster(context.system)
  import com.oceanum.common.Environment.NONE_BLOCKING_EXECUTION_CONTEXT

  override def preStart(): Unit = {
    cluster.subscribe(self, ClusterEvent.initialStateAsEvents, classOf[UnreachableMember], classOf[MemberEvent])
    fallback()
  }

  override def receive: Receive = {
    case m:MemberLeft =>
      fallback(m.member.uniqueAddress.address.host.getOrElse(""))
    case m:MemberDowned =>
      fallback(m.member.uniqueAddress.address.host.getOrElse(""))
    case m:MemberExited =>
      fallback(m.member.uniqueAddress.address.host.getOrElse(""))
    case m:MemberRemoved =>
      fallback(m.member.uniqueAddress.address.host.getOrElse(""))
  }

  override def postStop(): Unit = {
    cluster.unsubscribe(self)
    fallback()
  }

  def fallback(discard: String = ""): Unit = {

    val future: Future[ClusterNodes] = restService
      .clusterNodes(status = Some(NodeStatus.UP))
    future
      .flatMap { value: ClusterNodes =>
        coordinatorRepo
          .find(
            expr = "(!repo.terms('host', values)) && (!repo.exists('endTime') || (repo.field('endTime') > date.now()))",
            env = Map("values" -> value.nodes.map(_.host).filterNot(_.equals(discard)))
          )
          .map(_.filter(coord => coord.endTime.map(_.getTime).getOrElse(Long.MaxValue) > System.currentTimeMillis()))
          .flatMap { coordinators =>
            val seq: Seq[Future[Unit]] = coordinators
              .map(coord => {
                val host = value.consistentHashSelect(coord.name).host
                log.info("fallback: moving coordinator: [{}] to host: [{}]", coord, host)
                val newCoord = coord.copy(host = Option(host), version = coord.version + 1)
                val remoteRestService = RemoteRestServices.get(newCoord.host.get)
                remoteRestService.recover(newCoord)
              })
            Future.sequence(seq)
          }
      }
      .andThen {
        case Failure(exception) =>
          log.error(exception, "fallback failed: " + exception.getMessage)
      }
  }
}

object FallbackListener {
  def start(system: ActorSystem): ActorRef = {
    system.actorOf(ClusterSingletonManager.props(
      singletonProps = Props(classOf[FallbackListener]),
      settings = ClusterSingletonManagerSettings(system),
      terminationMessage = PoisonPill.getInstance
    ), name = "fallback-listener")
  }
}