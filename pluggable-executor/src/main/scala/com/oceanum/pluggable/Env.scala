package com.oceanum.pluggable

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

object Env {
  def conf(host: String) =
    s"""
       |akka {
       | actor {
       |   provider = "akka.remote.RemoteActorRefProvider"
       |   warn-about-java-serializer-usage = false
       |
       | }
       | remote {
       |    enabled-transports = ["akka.remote.netty.tcp"]
       |    log-remote-lifecycle-events = off
       |    netty.tcp {
       |      hostname = "${host}"
       |      port = 0
       |    }
       |  }
       |}
       |""".stripMargin

  def createSystem(host: String): ActorSystem = {
    ActorSystem("pluggable-executor", ConfigFactory.parseString(conf(host)))
  }

  @SerialVersionUID(1L)
  case class UpdateState(info: Map[String, String])
  @SerialVersionUID(1L)
  case object Kill
}
