package com.oceanum.triger

import java.util.Date

case class TriggerAction(action: Date => Unit)
