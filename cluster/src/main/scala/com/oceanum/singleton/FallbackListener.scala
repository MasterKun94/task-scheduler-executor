package com.oceanum.singleton

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, PoisonPill, Props}
import akka.cluster.ClusterEvent._
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings}
import akka.cluster.{Cluster, ClusterEvent}
import com.oceanum.api.RemoteRestServices
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
    log.info("start")
    recover()
  }

  override def postRestart(reason: Throwable): Unit = {
    log.info("restart")
    recover()
  }

  override def receive: Receive = {
    case m:MemberLeft =>
      recover(m.member.uniqueAddress.address.host.getOrElse(""))
    case m:MemberDowned =>
      recover(m.member.uniqueAddress.address.host.getOrElse(""))
    case m:MemberExited =>
      recover(m.member.uniqueAddress.address.host.getOrElse(""))
    case m:MemberRemoved =>
      recover(m.member.uniqueAddress.address.host.getOrElse(""))
    case _ => // discard
  }

  override def postStop(): Unit = {
    cluster.unsubscribe(self)
    log.info("stop")
    recover()
  }

  def recover(discard: String = ""): Unit = {

    val future: Future[ClusterNodes] = restService
      .clusterNodes(status = Some(NodeStatus.UP))
    future
      .flatMap { value: ClusterNodes =>
        coordinatorRepo
          .find(
            expr = "(!repo.terms('host', values)) && (!repo.exists('endTime') || (repo.field('endTime') > date.now()))",
            env = Map("values" -> value.nodes.map(_.host).filterNot(_.equals(discard)))
          )
          .flatMap { coordinators =>
            val seq: Seq[Future[Unit]] = coordinators
              .map(coord => {
                val host = value.consistentHashSelect(coord.name).host
                log.info("fallback: moving coordinator: [{}] to host: [{}]", coord.name, host)
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
    val listener = system.actorOf(ClusterSingletonManager.props(
      singletonProps = Props(classOf[FallbackListener]),
      settings = ClusterSingletonManagerSettings(system),
      terminationMessage = PoisonPill.getInstance
    ), name = "fallback-listener")
    listener
  }
}