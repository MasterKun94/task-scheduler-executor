package com.oceanum.api.entities

import java.util.Date

import com.oceanum.annotation.ISerializationMessage
import com.oceanum.common.{FallbackStrategy, GraphMeta, RerunStrategy}

@ISerializationMessage("RUN_WORKFLOW_INFO")
case class RunWorkflowInfo(id: Int,
                           name: String,
                           rerunId: Int,
                           fallbackStrategy: FallbackStrategy,
                           rerunStrategy: RerunStrategy,
                           createTime: Option[Date],
                           host: String)

object RunWorkflowInfo {
  def from(graphMeta: GraphMeta): RunWorkflowInfo = RunWorkflowInfo(
    id = graphMeta.id,
    name = graphMeta.name,
    rerunId = graphMeta.rerunId,
    fallbackStrategy = graphMeta.fallbackStrategy,
    rerunStrategy = graphMeta.rerunStrategy,
    createTime = graphMeta.createTime,
    host = graphMeta.host
  )
}
