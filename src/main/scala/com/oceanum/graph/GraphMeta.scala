package com.oceanum.graph

import java.util.Date

import com.oceanum.client.TaskMeta
import com.oceanum.common.Meta

trait GraphMeta[T<: GraphMeta[_]] extends Meta[T] {
  def id: Int
  def name: String
  def operators: Map[Int, TaskMeta[_]]
  def fallbackStrategy: FallbackStrategy.value
  def reRunStrategy: ReRunStrategy.value
  def graphStatus: GraphStatus.value
  def error: Throwable
  def createTime: Date
  def scheduleTime: Date
  def startTime: Date
  def endTime: Date
  def env: Map[String, Any]
}
