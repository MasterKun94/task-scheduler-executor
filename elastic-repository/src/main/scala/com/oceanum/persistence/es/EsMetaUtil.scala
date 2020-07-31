package com.oceanum.persistence.es

import com.oceanum.common.{GraphMeta, TaskMeta}

object EsMetaUtil {
  def convertGraphMeta(graphMeta: GraphMeta): EsGraphMeta = {
    new EsGraphMeta(
      id = graphMeta.id,
      name = graphMeta.name,
      reRunId = graphMeta.reRunId,
      tasks = graphMeta.tasks.values.map(getTaskMetaId(_, graphMeta)).toArray,
      latestTaskId = getTaskMetaId(graphMeta.tasks(graphMeta.latestTaskId), graphMeta),
      fallbackStrategy = graphMeta.fallbackStrategy,
      reRunStrategy = graphMeta.reRunStrategy,
      graphStatus = graphMeta.graphStatus,
      error = graphMeta.error,
      createTime = graphMeta.createTime,
      scheduleTime = graphMeta.scheduleTime,
      startTime = graphMeta.startTime,
      endTime = graphMeta.endTime,
      env = graphMeta.env
    )
  }

  def convertGraphMeta(graphMeta: EsGraphMeta, taskMetas: Map[String, TaskMeta]): GraphMeta = {
    new GraphMeta(
      id = graphMeta.id,
      name = graphMeta.name,
      reRunId = graphMeta.reRunId,
      tasks = taskMetas.filter(graphMeta.tasks.contains).values.map(m => (m.id, m)).toMap,
      latestTaskId = taskMetas(graphMeta.latestTaskId).id,
      fallbackStrategy = graphMeta.fallbackStrategy,
      reRunStrategy = graphMeta.reRunStrategy,
      graphStatus = graphMeta.graphStatus,
      error = graphMeta.error,
      createTime = graphMeta.createTime,
      scheduleTime = graphMeta.scheduleTime,
      startTime = graphMeta.startTime,
      endTime = graphMeta.endTime,
      env = graphMeta.env
    )
  }

  def getTaskMetaId(taskMeta: TaskMeta, graphMeta: GraphMeta): String = {
    s"${graphMeta.name}-${graphMeta.id}-${graphMeta.reRunId}-${taskMeta.id}"
  }

  def getGraphMetaId(graphMeta: GraphMeta): String = {
    s"${graphMeta.name}-${graphMeta.id}-${graphMeta.reRunId}"
  }
}
