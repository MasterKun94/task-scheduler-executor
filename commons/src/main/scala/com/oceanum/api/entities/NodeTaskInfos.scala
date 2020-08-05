package com.oceanum.api.entities

import com.oceanum.annotation.ISerializationMessage

/**
 * @author chenmingkun
 * @date 2020/8/5
 */
@SerialVersionUID(1L)
@ISerializationMessage("NODE_TASK_INFOS")
case class NodeTaskInfos(infos: Seq[NodeTaskInfo])
