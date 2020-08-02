package com.oceanum.api.entities

import com.oceanum.annotation.ISerializationMessage

/**
 * @author chenmingkun
 * @date 2020/8/2
 */
@ISerializationMessage("COORDINATOR")
case class Coordinator(name: String, trigger: TriggerConfig, workflowDefine: WorkflowDefine)
