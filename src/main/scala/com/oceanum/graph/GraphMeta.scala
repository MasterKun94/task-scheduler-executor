package com.oceanum.graph

import java.util.Date

import com.oceanum.client.TaskMeta
import com.oceanum.common.Meta

trait GraphMeta[T<: GraphMeta[_]] extends Meta[T] {
  def id: String
  def operators: Map[Int, TaskMeta[_]]
  def fallbackStrategy: FallbackStrategy.value
  def reRunStrategy: ReRunStrategy.value
  def graphStatus: GraphStatus.value
  def error: Throwable
  def createTime: Date
  def startTime: Date
  def endTime: Date
}
