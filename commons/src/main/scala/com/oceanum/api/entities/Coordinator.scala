package com.oceanum.api.entities

import java.util.Date

import com.oceanum.annotation.ISerializationMessage
import com.oceanum.common.{Environment, FallbackStrategy}

/**
 * @author chenmingkun
 * @date 2020/8/2
 */
@ISerializationMessage("COORDINATOR")
case class Coordinator(name: String,
                       fallbackStrategy: FallbackStrategy,
                       trigger: TriggerConfig,
                       workflowDefine: WorkflowDefine,
                       startTime: Option[Date] = None,
                       endTime: Option[Date] = None,
                       host: Option[String] = None,
                       version: Int)
