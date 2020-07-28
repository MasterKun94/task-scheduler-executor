package com.oceanum.exec.runners

import com.oceanum.exec.{ExecutionTask, ExitCode, TypedRunner}
import com.oceanum.exec.tasks.PluggableTaskConfig

object PluggableProcessRunner extends TypedRunner[PluggableTaskConfig] {
  private val innerRunner = ProcessRunner

  override protected def typedRun(task: ExecutionTask[_ <: PluggableTaskConfig]): ExitCode = {
    ???
  }

  override def close(): Unit = ???
}
