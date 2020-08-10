package com.oceanum.exec.runners

import akka.actor.Props
import com.oceanum.common.ActorSystems
import com.oceanum.exec.tasks.{PluggableTaskConfig, ProcessTaskConfig}
import com.oceanum.exec.{ExecutionTask, ExitCode, TypedRunner}
import com.oceanum.pluggable.{PluggablePrimEndpoint, PrimListener}

import scala.collection.concurrent.TrieMap

object PluggableProcessRunner extends TypedRunner[PluggableTaskConfig] {
  private val innerRunner = ProcessRunner

  override protected def typedRun(task: ExecutionTask[_ <: PluggableTaskConfig]): ExitCode = {

    val allInfo: TrieMap[String, String] = TrieMap()

    val listener = new PrimListener {
      override def updateState(info: Map[String, String]): Unit = {
        allInfo ++= info
        task.eventListener.running(task.metadata.copy(extendedProperties = allInfo.toMap))
      }
    }
    val prim = ActorSystems.SYSTEM.actorOf(Props(classOf[PluggablePrimEndpoint], listener))
    val processTask: ExecutionTask[_<:ProcessTaskConfig] = task.copy(prop = task.prop.toJavaTaskConfig(prim))

    innerRunner.run(processTask)
  }

  override def close(): Unit = {}
}
