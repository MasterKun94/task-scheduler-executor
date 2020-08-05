package com.oceanum.api.entities

import com.oceanum.annotation.ISerializationMessage
import com.oceanum.common.Environment

/**
 * @author chenmingkun
 * @date 2020/8/2
 */
@ISerializationMessage("WORKFLOW_DEFINE")
case class WorkflowDefine(version: Int,
                          name: String,
                          dag: Dag,
                          env: Map[String, Any],
                          host: String = Environment.HOST,
                          alive: Boolean = false)
