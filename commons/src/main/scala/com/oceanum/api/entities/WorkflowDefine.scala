package com.oceanum.api.entities

import com.oceanum.annotation.ISerializationMessage

/**
 * @author chenmingkun
 * @date 2020/8/2
 */
@ISerializationMessage("WORKFLOW_DEFINE")
case class WorkflowDefine(name: String, dag: Dag, env: Map[String, Any])
