package com.oceanum.client

import java.util.Date

import com.oceanum.cluster.exec.State
import com.oceanum.common.Meta

trait TaskMeta[T<:TaskMeta[_]] extends Meta[T] {
  def id: Int
  def taskType: String
  def user: String
  def createTime: Date
  def startTime: Date
  def endTime: Date
  def execDir: String
  def message: String
  def error: Throwable
  def state: State.value
  def retryNum: Int
}
