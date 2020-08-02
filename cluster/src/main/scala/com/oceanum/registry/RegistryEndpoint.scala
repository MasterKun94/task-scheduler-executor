package com.oceanum.registry

import akka.actor.{Actor, ActorLogging, Address}

import scala.collection.mutable

/**
 * @author chenmingkun
 * @date 2020/8/2
 */
class RegistryEndpoint extends Actor with ActorLogging {
  private val registry: mutable.Map[String, Address] = mutable.Map()

  override def receive: Receive = {
    case _ => registry("").host

  }
}
