package com.oceanum.api

import scala.collection.concurrent.TrieMap

case object RemoteRestServices {
  private val remoteRestServices: TrieMap[String, RemoteRestService] = TrieMap()
  def get(host: String): RemoteRestService = remoteRestServices.getOrElseUpdate(host, new RemoteRestService(host))
}
