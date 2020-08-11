package com.oceanum.pluggable

trait PrimListener {
  def updateState(info: Map[String, String]): Unit
}
