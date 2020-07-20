package com.oceanum.cluster.runners

import com.oceanum.cluster.exec.{ExecutionTask, ExitCode, TypedRunner}

/**
 * @author chenmingkun
 * @date 2020/7/20
 */
class SparkRunner extends TypedRunner[SparkRunner] {
  override protected def typedRun(task: ExecutionTask[_ <: SparkRunner]): ExitCode = ???

  override def close(): Unit = ???
}
