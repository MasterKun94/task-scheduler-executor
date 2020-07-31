package com.oceanum.pluggable

trait Executor {
  def startRun(): Unit

  def kill()
}
