package com.oceanum.persistence

import com.oceanum.common.{GraphMeta, TaskMeta}

import scala.concurrent.Future

object PersistenceUtil {

  def save(graphMeta: GraphMeta): Future[Unit] = ???

  def save(taskMeta: TaskMeta): Future[Unit] = ???
}
