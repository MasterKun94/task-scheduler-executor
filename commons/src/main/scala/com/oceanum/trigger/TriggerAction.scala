package com.oceanum.trigger

import java.util.Date

case class TriggerAction(action: (Date, Map[String, Any]) => Unit)
