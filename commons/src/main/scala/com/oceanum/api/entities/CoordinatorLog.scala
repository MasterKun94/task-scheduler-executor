package com.oceanum.api.entities

import java.util.Date

import com.oceanum.annotation.ISerializationMessage

/**
 * @author chenmingkun
 * @date 2020/8/2
 */
@ISerializationMessage("COORDINATOR_LOG")
case class CoordinatorLog(name: String,
                          workflowName: String,
                          workflowId: Option[Int],
                          timestamp: Date,
                          workflowSubmitted: Boolean,
                          error: Option[Throwable])
