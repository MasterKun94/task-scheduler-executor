package com.oceanum.registry

import akka.actor.{Actor, ActorLogging, ActorSystem, PoisonPill, Props}
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings, ClusterSingletonProxy, ClusterSingletonProxySettings}
import com.oceanum.common.Environment
import com.typesafe.config.ConfigFactory

class SingletonActor extends Actor with ActorLogging {
  import SingletonActor._
  import akka.cluster._


  override def receive: Receive = {
    case Greeting(msg) =>
      log.info("*********got {} from {}********", msg, sender().path.address)
    case Relocate =>
      log.info("*********I'll move from {}********", self.path.address)
      val cluster = Cluster(context.system)
      cluster.leave(cluster.selfAddress)
    case Die =>
      log.info("*******I'm shutting down ... ********")
      self ! PoisonPill
  }
}

object SingletonActor {
  trait SingletonMsg {}
  case class Greeting(msg: String) extends SingletonMsg
  case object Relocate extends SingletonMsg
  case object Die extends SingletonMsg

  def props = Props(new SingletonActor)

  def createSingleton(port: Int) = {
//  val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port")
//  .withFallback(ConfigFactory.parseString("akka.cluster.roles=[singleton]"))
//  .withFallback(ConfigFactory.load())
  val singletonSystem = Environment.CLUSTER_NODE_SYSTEM

  val singletonManager = singletonSystem.actorOf(ClusterSingletonManager.props(
  singletonProps = props,
  terminationMessage = Die,
  settings = ClusterSingletonManagerSettings(singletonSystem)
  ),
  name = "singletonManager"
  )
}

}

object SingletonUser {
  import SingletonActor._
  def sendToSingleton(msg: SingletonMsg) = {
    val config = ConfigFactory.parseString("akka.cluster.roles=[greeter]")
      .withFallback(ConfigFactory.load())

    val system = Environment.CLIENT_SYSTEM
    val singletonProxy = system.actorOf(ClusterSingletonProxy.props(
      "user/singletonManager",
      ClusterSingletonProxySettings(system).withRole(None)
    ))

    singletonProxy ! msg
  }
}

import SingletonActor._
import SingletonUser._
object SingletonDemo extends App {

  createSingleton(5551)    //seednode
  createSingleton(5552)
  createSingleton(5553)
  createSingleton(5554)

  scala.io.StdIn.readLine()

  sendToSingleton(Greeting("hello from tiger"))
  scala.io.StdIn.readLine()

  sendToSingleton(Relocate)
  scala.io.StdIn.readLine()

  sendToSingleton(Greeting("hello from cat"))
  scala.io.StdIn.readLine()

  sendToSingleton(Die)
  scala.io.StdIn.readLine()

}