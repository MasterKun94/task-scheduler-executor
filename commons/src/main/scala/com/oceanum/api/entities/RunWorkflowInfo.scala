package com.oceanum.api.entities

import java.util.Date

import com.oceanum.annotation.ISerializationMessage
import com.oceanum.common.{FallbackStrategy, GraphMeta, ReRunStrategy}

@ISerializationMessage("RUN_WORKFLOW_INFO")
case class RunWorkflowInfo(id: Int,
                           name: String,
                           reRunId: Int,
                           fallbackStrategy: FallbackStrategy,
                           reRunStrategy: ReRunStrategy,
                           createTime: Option[Date])

object RunWorkflowInfo {
  def from(graphMeta: GraphMeta): RunWorkflowInfo = RunWorkflowInfo(
    id = graphMeta.id,
    name = graphMeta.name,
    reRunId = graphMeta.reRunId,
    fallbackStrategy = graphMeta.fallbackStrategy,
    reRunStrategy = graphMeta.reRunStrategy,
    createTime = graphMeta.createTime
  )
}
