package com.oceanum.api.entities

import com.oceanum.annotation.ISerializationMessage

@SerialVersionUID(1L)
@ISerializationMessage("NODE_TASK_INFO")
case class NodeTaskInfo(host: String, topics: Seq[String], preparing: Int, running: Int, success: Int, failed: Int, retry: Int, killed: Int, complete: Int)
