package com.oceanum.triger

import scala.collection.concurrent.TrieMap

object Triggers {
  private val triggers: TrieMap[String, Trigger] = TrieMap()

  def getTrigger(triggerType: String): Trigger = {
    triggers(triggerType.toUpperCase())
  }

  def addTrigger(trigger: Trigger): Unit = {
    triggers += (trigger.triggerType.toUpperCase() -> trigger)
  }
}
