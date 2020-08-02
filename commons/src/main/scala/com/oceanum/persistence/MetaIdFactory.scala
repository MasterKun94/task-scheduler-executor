package com.oceanum.persistence

import com.oceanum.common.{GraphMeta, TaskMeta}

/**
 * @author chenmingkun
 * @date 2020/7/31
 */
object MetaIdFactory {

  def getTaskMetaId(graphMeta: GraphMeta, taskMeta: TaskMeta): String = {
    s"${graphMeta.name}-${graphMeta.id}-${taskMeta.reRunId}-${taskMeta.id}"
  }

  def getGraphMetaId(graphMeta: GraphMeta): String = {
    s"${graphMeta.name}-${graphMeta.id}-${graphMeta.reRunId}"
  }
}
