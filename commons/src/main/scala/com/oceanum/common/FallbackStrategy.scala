package com.oceanum.common

object FallbackStrategy extends Enumeration {
    type value = Value
    val CONTINUE, SHUTDOWN = Value
  }
