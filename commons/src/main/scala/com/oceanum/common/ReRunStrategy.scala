package com.oceanum.common

object ReRunStrategy extends Enumeration {
    type value = Value
    val NONE, RUN_ALL, RUN_ONLY_FAILED, RUN_ALL_AFTER_FAILED = Value
}
