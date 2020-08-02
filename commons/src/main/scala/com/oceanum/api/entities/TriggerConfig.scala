package com.oceanum.api.entities

import com.oceanum.annotation.ISerializationMessage

/**
 * @author chenmingkun
 * @date 2020/8/2
 */
@ISerializationMessage("TRIGGER_CONFIG")
case class TriggerConfig(name: String, config: Map[String, String])
